/*
 * Copyright (c) 2002-2006 The MITRE Corporation
 * 
 * Except as permitted below
 * ALL RIGHTS RESERVED
 * 
 * The MITRE Corporation (MITRE) provides this software to you without
 * charge to use for your internal purposes only. Any copy you make for
 * such purposes is authorized provided you reproduce MITRE's copyright
 * designation and this License in any such copy. You may not give or
 * sell this software to any other party without the prior written
 * permission of the MITRE Corporation.
 * 
 * The government of the United States of America may make unrestricted
 * use of this software.
 * 
 * This software is the copyright work of MITRE. No ownership or other
 * proprietary interest in this software is granted you other than what
 * is granted in this license.
 * 
 * Any modification or enhancement of this software must inherit this
 * license, including its warranty disclaimers. You hereby agree to
 * provide to MITRE, at no charge, a copy of any such modification or
 * enhancement without limitation.
 * 
 * MITRE IS PROVIDING THE PRODUCT "AS IS" AND MAKES NO WARRANTY, EXPRESS
 * OR IMPLIED, AS TO THE ACCURACY, CAPABILITY, EFFICIENCY,
 * MERCHANTABILITY, OR FUNCTIONING OF THIS SOFTWARE AND DOCUMENTATION. IN
 * NO EVENT WILL MITRE BE LIABLE FOR ANY GENERAL, CONSEQUENTIAL,
 * INDIRECT, INCIDENTAL, EXEMPLARY OR SPECIAL DAMAGES, EVEN IF MITRE HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You accept this software on the condition that you indemnify and hold
 * harmless MITRE, its Board of Trustees, officers, agents, and
 * employees, from any and all liability or damages to third parties,
 * including attorneys' fees, court costs, and other related costs and
 * expenses, arising out of your use of this software irrespective of the
 * cause of said liability.
 * 
 * The export from the United States or the subsequent reexport of this
 * software is subject to compliance with United States export control
 * and munitions control restrictions. You agree that in the event you
 * seek to export this software you assume full responsibility for
 * obtaining all necessary export licenses and approvals and for assuring
 * compliance with applicable reexport restrictions.
 */

package org.mitre.ace2004.callisto;

import java.util.*;
import java.net.URI;

import gov.nist.atlas.*;
import gov.nist.atlas.type.ATLASType;
import gov.nist.atlas.type.AnnotationType;

import org.mitre.jawb.atlas.*;
import org.mitre.jawb.gui.JawbDocument;
import org.mitre.jawb.tasks.Task;

/**
 * Manipulator of ACE2004 ID attributes, which must remain constant over
 * saving/loading/importing/exporting to APF, and AIF both.<p>
 *
 * "Spec" (or "slightly-less-than-arbitrary-specification") of how ID's are
 * handled in this task.
 * 
 * <ol>
 
 *   <li>If APF is being generated for the first time by Callisto.  All
 *   entities, mentions, relations, and relation-mentions conform to the *
 *   following syntax:<p>
 *   "RDC" ID conventions
 *   <code>
 *     <table>
 *       <tr><td>entity ::= <td>[docid]-E[entity-num]
 *       <tr><td>mention ::= <td>[entity-id]-[mention-num]
 *       <tr><td>relation ::= <td>[docid]-R[relation-num]
 *       <tr><td>relation-mention ::= <td>[relaion-id]-[relation-mention-num]
 *       <tr><td>event ::= <td>[docid]-V[event-num]
 *       <tr><td>event-mention ::= <td>[event-id]-[event-mention-num]
 *       <tr><td>quantity ::= <td>[docid]-R[quantity-num]
 *       <tr><td>quantity-mention ::= <td>[quantity-id]-[quantity-mention-num]
 *     </table>
 *   </code>
 *
 *   <li>If APF is being imported <i>and</i> it conforms to the "RDC" ID
 *   conventions (as in (1)), then parse accordingly.  All <i>unmodified</i>
 *   entities, mentions, relations, and rel-mentions will retain their IDs.  If
 *   <i>anything</i> about a mention or rel-mention is changed, then it's ID
 *   will be changed to conform to (1) (by incrementing the xxx-num counter to
 *   1 larger than largest valid value encountered so far).  If any attribute
 *   (not mentions) of an entity or relation is changed, then it's ID will be
 *   regenerated according to (1).  Whenever an entity or relation ID changes,
 *   then all of its mention/rel-mention IDs will change as well.
 * 
 *   <li>If APF is being imported and it does <i>not</i> conform to the RDC
 *   naming convention, then retain all IDs "as long as possible."
 *   Regeneration of IDs will occur as in (2). Of note is that "hybrid IDs"
 *   could occur for mentions and rel-mentions which have been reassigned to
 *   entities or relations which do not conform to (1).  These
 *   entities/relations, may then have two different naming schemes for their
 *   contained mentions/rel-mentions: some as imported, some hybrid.<p>
 * 
 *   Imported entites/mentions which require updaed IDs according to (2) will
 *   get new IDs conforming to (1).
 * </ol>
 *
 * <h2>Implementation</h3>
 *
 * This is all achieved by maintainting a few runtime client properties for
 * each document (see {@link AWBDocument#setClientProperty}, and listening to
 * annotation changes/additions/deletions to assign/reassign IDs.
 *
 * <dl>
 *   <dt>{@link #ID_TRACKER}
 *   <dd>An instance of this class to listen to changes/additions/removals from
 *       the document and assign/reassign IDs accordingly. This ensures it is
 *       garbage collected with the document.
 *   <dt>{@link #DOCID}
 *   <dd>The original document id as read from an imported document, or the file
 *       name of the original SGML/text file.
 *   <dt>{@link #LAST_ID_MAP}
 *   <dd>Maintains counters for the various types for which we generate
 *       IDs. When an ID is created/found, the lookup value (for peers of the
 *       annot beeing looked up) is updated to be the greatest seen.
 *   <dd>
 * </dl>
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class IdTracker {
  
  private static final int DEBUG = 0;

  /** Used to maintain unique APF ID's at runtime */
  public static final Object DOC_SOURCE = "DOC_SOURCE";
  public static final Object DOC_URI = "DOC_URI";
  public static final Object DOCID = "DOCID";
  public static final Object LAST_ID_MAP = "LAST_ID_MAP";
  public static final Object ID_TRACKER = "ID_TRACKER";

  /** Map type names to the 'prefix' used to create the ID */
  private static Map idPrefixMap = new HashMap();

  static {
    // initialize the idMap with these prefixes
    // entity has not traditionally been given a prefix.
    idPrefixMap.put(ACE2004Task.ENTITY_TYPE_NAME, "E");
    idPrefixMap.put(ACE2004Task.RELATION_TYPE_NAME, "R");
    idPrefixMap.put(ACE2004Task.QUANTITY_TYPE_NAME, "Q");
    idPrefixMap.put(ACE2004Task.EVENT_TYPE_NAME, "V");
    idPrefixMap.put(ACE2004Task.TIMEX2_TYPE_NAME, "T");
  }
  
  private AWBDocument doc = null;

  private IdTracker (AWBDocument doc) {
    this.doc = doc;
    // if any already have RDC ID's we need to register them
    Iterator iter = doc.getAllAnnotations();
    while (iter.hasNext()) {
      AWBAnnotation annot = (AWBAnnotation)iter.next();
      String aceId = (String) annot.getAttributeValue ("ace_id");
      if (aceId != null && ! aceId.equals (""))
        registerAceId (annot, aceId);
    }
    // now listen for changes
    doc.addAnnotationModelListener(new AnnotModelListener());
  }

  /**
   * Makes sure any doc only has one, no matter how many windows it's displayed in.
   */
  public static final IdTracker getIdTracker (AWBDocument doc) {
    IdTracker idTracker = (IdTracker) doc.getClientProperty (IdTracker.ID_TRACKER);
    if (idTracker == null) {
      idTracker = new IdTracker (doc);
      doc.putClientProperty (IdTracker.ID_TRACKER, idTracker);
    }
    return idTracker;
  }

  /*
   * ID's are written as <docid>-<annotid> in APF output, and this must be
   * stored in the AIF as well. We store the largest integer used for a group
   * (e.g., all entities, all mentions w/in an entity), as well as the ID sans
   * "<docid>-" (e.g., "1", "1-1", "RDCID-1") mapped to the annot it's the ID
   * for. Then we can be assured that:
   *  1) we write out the same IDs we read in
   *  2) we won't create a duplicate
   *  3) we can do a fast lookup to display just the ID (see RelationEditor)
   */
  private static final Map getLastIdMap (AWBDocument doc) {
    Map lastIdMap = (Map) doc.getClientProperty (LAST_ID_MAP);
    if (lastIdMap == null) {
      lastIdMap = new HashMap();
      doc.putClientProperty (LAST_ID_MAP, lastIdMap);
    }
    return lastIdMap;
  }

  /**
   * Returns null, or the prefix string for the annot type.  Note that Mention
   * types don't get prefixes.
   */
  protected static String getIdPrefix (AnnotationType type) {
    if (type != null)
      return (String) idPrefixMap.get(type.getName());
    return null;
  }

  protected static final String getDocId (AWBDocument doc) {
    String docId = (String) doc.getClientProperty (DOCID);
    if (docId == null) { // doc wasn't from import: figure it out
      //System.err.println ("  DOCID="+docId);
      URI signalURI = (URI) doc.getClientProperty(DOC_URI);
      //System.err.println ("  DOC_URI="+signalURI);
      if (signalURI == null)
        signalURI = doc.getSignalURI ();

      String file = signalURI.getPath();
      docId = file.substring(file.lastIndexOf('/')+1); // -1 OK!
      // HACK remove the file extension
      try {
        docId = docId.substring(0,docId.lastIndexOf('.'));
      } catch (IndexOutOfBoundsException e) {}
      doc.putClientProperty (DOCID, docId);
    }
    //System.err.println ("  DOCID="+docId);
    return docId;
  }
  
  /**
   * Tries to parse an APF ID and update the {@link #LAST_ID_MAP}.  This
   * ensures that no ID's are reused, by makeing sure that the counter stored
   * in 'lastIDMap' is greater than whatever we see.
   * @see #getNextId
   */
  protected final void registerAceId (AWBAnnotation annot, String aceId) {
    if (DEBUG > 0)
      System.err.println("IdTracker.registerAceId: " + aceId + 
                         " for annot of type " + 
                         annot.getAnnotationType().getName());

    // Full ID is determined by type.
    ATLASType type = annot.getAnnotationType();

    // get the counter for the element (number after final "-")
    int lastDelim = aceId.lastIndexOf ("-");
    int i;
    // for TIMEX2, if the element after the final "-" is just 1,
    // get the item after the previous "-" instead
    if (type == ACE2004Utils.TIMEX2_TYPE && 
        "-1".equals(aceId.substring(lastDelim))) {
      if (DEBUG > 0)
        System.err.println ("registerAceId found Timex2 with -1 ending: " +
                            aceId);
      i = aceId.lastIndexOf ("-", lastDelim-1) +1;
    } else {
      i = lastDelim+1;
    }

    // variable size prefixes, only some ending in '-' (e.g. E1-1 RDC-1 QID1-2)
    while (i<aceId.length() && ! Character.isDigit(aceId.charAt(i)))
      i++;

    String counterString;
    if (i < lastDelim) { // type == ACE2004Utils.TIMEX2_TYPE with -1 ending
      counterString = aceId.substring (i, lastDelim);
    } else {
      counterString = aceId.substring (i);
    }

    Integer counter = null;
    try {
      counter = Integer.valueOf (counterString);
    } catch (NumberFormatException n) {} // catch via (counter==null)

    if (counter == null || lastDelim < 1) {
      System.err.println ("Unable to register "+aceId+
                          "\n  id's may seem to overlap, but will be unique");
      return;
    }

    if (DEBUG > 1)
      System.err.println("registerAceId registerig counter: " + counter +
                         " for " + aceId);

    // since mentions and relMentions have incremental ID's within their
    // respective entities and relaions, we store the 'greatest' counter by
    // superordinate for mentions and mention-relations, but by the
    // AnnotationType for entities and relations. Here we determine that key,
    // and find the actual 'id' (sans docid)
    Object idLookupKey = null;

    if (type.equals(ACE2004Utils.ENTITY_MENTION_TYPE) ||
        type.equals(ACE2004Utils.RELATION_MENTION_TYPE) ||
        type.equals(ACE2004Utils.QUANTITY_MENTION_TYPE) ||
        type.equals(ACE2004Utils.EVENT_MENTION_TYPE)) {
      ACE2004Task task = (ACE2004Task) doc.getTask();
      idLookupKey = task.getMentionParent(annot);
      if (idLookupKey == null) {
        System.err.println("Annot missing Superordinate: " + annot.getId().getAsString());
        return;
      }
      
    } else if (type.equals(ACE2004Utils.ENTITY_TYPE) ||
               type.equals(ACE2004Utils.RELATION_TYPE) ||
               type.equals(ACE2004Utils.QUANTITY_TYPE) ||
               type.equals(ACE2004Utils.TIMEX2_TYPE) ||
               type.equals(ACE2004Utils.EVENT_TYPE)) {
      idLookupKey = type;
      
    } else {
      System.err.println ("IdTracker: illegal id registration for: "+annot);
      return;
    }

    // get the annot counter and make sure the largest is in the lastIDMap
    Map lastIdMap = getLastIdMap (doc);

    Integer greatest = (Integer) lastIdMap.get (idLookupKey);
    if (greatest == null || counter.intValue() > greatest.intValue())
      lastIdMap.put (idLookupKey, counter);

    if (DEBUG > 0)
      System.err.println("IdTracker.regAceId: lastId is now: " + 
                         lastIdMap.get (idLookupKey));

    try {
      annot.setAttributeValue ("ace_id", aceId);
    } catch (UnmodifiableAttributeException x) {}
    
    return;
  }

  /**
   * Retrieve or assign an ID for the specified annotation. Generated ID's
   * will be prefixed with the DOCID, as per the specification in the class
   * documentation. This is mostly to keep ID's constant across import/exports,
   * since we cannot specify the id's in jATLAS when importing.
   * @return the ID for the annot, or null if it has neither been set, nor can
   *    be, as in the case of mentions w/out entities.
   */
  protected final String getAceId (AWBAnnotation annot) {
    if (DEBUG > 0)
      System.err.println("IdTracker.getAceId for annot of type " +
                         annot.getAnnotationType().getName());
    String aceId = (String) annot.getAttributeValue ("ace_id");
    if (aceId == null || aceId.equals(""))
      return assignAceId (annot);
    if (DEBUG > 0)
      System.err.println("IdTracker.getAceIdS returning " + aceId);
    return aceId;
  }

  /**
   */
  private String assignAceId (AWBAnnotation annot) {
    if (DEBUG > 0)
      System.err.println("  IdTracker.assignAceId for annot of type " +
                         annot.getAnnotationType().getName());
    String aceId = null;
    // Full ID is determined by type.
    AnnotationType type = annot.getAnnotationType();
    
    if (type.equals(ACE2004Utils.ENTITY_MENTION_TYPE) ||
        type.equals(ACE2004Utils.RELATION_MENTION_TYPE) ||
        type.equals(ACE2004Utils.QUANTITY_MENTION_TYPE) ||
        type.equals(ACE2004Utils.EVENT_MENTION_TYPE)) {
      ACE2004Task task = (ACE2004Task) doc.getTask();
      AWBAnnotation parent = task.getMentionParent(annot);
      if (parent == null)
        return null; // mention wouldn't be written out anyway.
      String parentid = getAceId(parent);
      aceId = parentid + "-" + getNextId(parent);

    } else if (type.equals(ACE2004Utils.ENTITY_TYPE) ||
               type.equals(ACE2004Utils.RELATION_TYPE) ||
               type.equals(ACE2004Utils.QUANTITY_TYPE) ||
               type.equals(ACE2004Utils.EVENT_TYPE)) {
      StringBuffer buf = new StringBuffer(getDocId(doc)).append("-");
      String prefix = getIdPrefix(type);
      if (prefix != null)
        buf.append(prefix);
      buf.append(getNextId(type));
      aceId = buf.toString();
      
    } else if (type.equals(ACE2004Utils.TIMEX2_TYPE)) {
      StringBuffer buf = new StringBuffer(getDocId(doc)).append("-");
      String prefix = getIdPrefix(type);
      if (prefix != null) {
	  buf.append(prefix);      // add "T"   to create "<docid>-T"
      }
      int tid = getNextId(type);
      buf.append(tid);             // add <int> to create "<docid>-T<int>"
      // Note: At this point in time
      // there is only one mention for
      // each timex2 -- the aceid is the "mention id" for the timex2
      // the timex2id will lop off this -1
      buf.append("-1");            // add "-"   to create "<docid>-T<int>-1"
      aceId = buf.toString();
      
    } else { // reltimes and time ranges don't get IDs
      System.err.println ("IdTracker: illegal request for id: "+annot);
      return null;
    }

    try {
      annot.setAttributeValue ("ace_id", aceId);
    } catch (UnmodifiableAttributeException x) {}
    
    if (DEBUG > 0)
      System.err.println("  IdTracker.assignAceId returning " + aceId);

    return aceId;
  }
  
  /**
   * Returns and id for the next annotation of the specified type or
   * subordinate. For a 'containing' annotation (Entity, Relation) the
   * 'typeOrSuper' should be the AnnotationType object for that type. For a
   * 'contained' annotation where the id number is derived from the containing
   * annotation (Mention, Mention-Relation), 'typeOrSuper' should be the
   * superordinate which the next id should be generated for. Note that if
   * sub-ordinates are reassigned to other super-ordinates this derivation of
   * ID's will be broken, as ID's are never changed.
   */
  private int getNextId (Object typeOrSuper) {
    if (DEBUG > 0)
      System.err.println("    IdTracker.getNextId: " + typeOrSuper);

    Map lastIdMap = getLastIdMap (doc);

    Integer counter = (Integer) lastIdMap.get (typeOrSuper);
    if (counter == null)
      counter = new Integer (1); // don't start at 0 (why?)
    else
      counter = new Integer (counter.intValue()+1);
    
    lastIdMap.put (typeOrSuper, counter);
    if (DEBUG > 0)
      System.err.println("    IdTracker.getNextId returning " + 
                         counter.intValue());
    return counter.intValue();
  }

  /***********************************************************************/
  /* Implementing the AnnotationModelListener Interface */
  /***********************************************************************/

  /**
   * Update the 'mention-count' row of entities when mentions are removed
   */
  private static class AnnotModelListener implements AnnotationModelListener {
    public void annotationCreated (AnnotationModelEvent e) {
      // was going to set the ENTITY and RELATION type IDs here, but they will
      // be set when the subordinates are added because the subordinate relies
      // on the ID of the superordinate!
    }
    public void annotationDeleted (AnnotationModelEvent e) {}
    public void annotationChanged (AnnotationModelEvent e) {}
    public void annotationInserted (AnnotationModelEvent e) {
      AWBAnnotation annot = e.getAnnotation ();
      ATLASType type = annot.getAnnotationType ();
      // set the ID of mentions/rel-mentions when added to their parent
      if (type.equals (ACE2004Utils.ENTITY_TYPE) ||
          type.equals (ACE2004Utils.RELATION_TYPE)) {
        IdTracker idTracker = IdTracker.getIdTracker((AWBDocument)e.getModel());
        AWBAnnotation[] mention = e.getChange().getAnnotationsInserted();
        
        for (int i=0; i<mention.length; i++)
          idTracker.getAceId (mention[i]); // RK 9/13/05 changed from
                                           // assignAceId in case the
                                           // mention has already been
                                           // assigned an id by the
                                           // ACE2004AnnotationListener
                                           // in ACE2004ToolKit
      }
    }
    public void annotationRemoved (AnnotationModelEvent e) {
      AWBAnnotation annot = e.getAnnotation ();
      ATLASType type = annot.getAnnotationType ();
      // clear the ID of mentions/rel-mentions when removed from their parent
      if (type.equals (ACE2004Utils.ENTITY_TYPE) ||
          type.equals (ACE2004Utils.RELATION_TYPE)) {
        AWBAnnotation[] mention = e.getChange().getAnnotationsRemoved();
        for (int i=0; i<mention.length; i++) {
          try { mention[i].setAttributeValue ("ace_id", null);
          } catch (UnmodifiableAttributeException x) {}
        }
      }
    }
  }
}


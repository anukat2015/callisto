
/* ----------------------------------------------------------------------
 * 
 * Copyright (c) 2002-2009 The MITRE Corporation
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
 * 
 * ----------------------------------------------------------------------
 * 
 * NOTICE
 * 
 * This software was produced for the U. S. Government
 * under Contract No. W15P7T-09-C-F600, and is
 * subject to the Rights in Noncommercial Computer Software
 * and Noncommercial Computer Software Documentation
 * Clause 252.227-7014 (JUN 1995).
 * 
 * (c) 2009 The MITRE Corporation. All Rights Reserved.
 * 
 * ----------------------------------------------------------------------
 *
 */
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

package org.mitre.jawb.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;
import javax.swing.text.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * JTabbedPane implementation which allows tabbs to be 'torn off' as their own
 * window. When the DetachableTabbedPane is set not visible using the
 * 'setVisible' method, any detached tabs are also hidden. When set visible by
 * the same means, previously detached, yet hidden tabs, are re-shown.
 *
 * @author <a href="mailto:red@mitre.org">Chadwick A. McHenry</a>
 * @version 1.0
 */
public class DetachableTabbedPane extends JTabbedPane {

  /* multiple use Icons */
  private static Icon plainIcon = new DetachPanelIcon (false);
  private static Icon pressedIcon = new DetachPanelIcon (true);
  /* map panels to their Detachable objects
   * @see #Detachable */
  protected HashMap panelToDetMap = new HashMap ();

  /**
   * Indicates whether the tabs in this TabbedPane are actually detachable,
   * or just behave normally
   */
  protected boolean detachable = true;

  /** Prettify the detached tabbs */
  protected Image detachedIconImage = null;
  
  /**
   * Creates an empty <code>DetachableTabbedPane</code> with a default tab
   * placement of <code>JTabbedPane.TOP</code> and detachability on.
   */
  public DetachableTabbedPane () {
    super ();
    init ();
  }
  
  /**
   * Creates an empty <code>DetachableTabbedPane</code> with the specified tab
   * placement of either: <code>JTabbedPane.TOP</code>,
   * <code>JTabbedPane.BOTTOM</code>, <code>JTabbedPane.LEFT</code>, or
   * <code>JTabbedPane.RIGHT</code>, and specified detachability.
   */
  public DetachableTabbedPane (int tabPlacement, boolean detachable) {
    super (tabPlacement);
    this.detachable = detachable;
    init ();
  }
  
  /**
   * Creates an empty <code>DetachableTabbedPane</code> with the specified tab
   * placement and tab layout policy.
   */
  public DetachableTabbedPane (int tabPlacement, int tabLayoutPolicy,
                               boolean detachable) {
    super (tabPlacement, tabLayoutPolicy);
    this.detachable = detachable;
    init ();
  }

  /** Code common to all constructors. */
  private void init () {
    // retrieve the current mouse listeners (put in by L&F) and remove them
    // from the standard dispatcher so we can filter some events
    final MouseListener[] mListeners = getMouseListeners ();
    for (int i=0; i<mListeners.length; i++)
      removeMouseListener (mListeners[i]);

    // this will forward mouse events to the little detach buttons and
    // all the look and feel listeners (since we want to filter some)
    MouseInputAdapter ma = new MouseInputAdapter () {
        Detachable last = null;
        // Returns a Detachable only if the mouse event is within the
        // detachable's icon
        private Detachable getDetachable (MouseEvent e) {
          if (last != null && last.contains (e.getX(), e.getY()))
            return last;

          last = null;
          Iterator iter = panelToDetMap.values ().iterator ();
          while (iter.hasNext ()) {
            Detachable d = (Detachable) iter.next();
            if (d.contains (e.getX(), e.getY())) {
              last = d;
              break;
            }
          }
          return last;
        }
        public void mouseMoved (MouseEvent e) {
          Detachable old = last;
          Detachable d = getDetachable (e);
          if (old != d) {
            if (old != null) {
              old.setPressed (false);
              old.repaint ();
            }
            if (d != null) {
              d.setPressed (true);
              d.repaint ();
            }
          }
        }
        public void mouseClicked (MouseEvent e) {
          Detachable d = getDetachable (e);
          last = null;
          if (d != null) {
            detach (d);
            d.setPressed (false);
            // filter the event from the other handlers
            return;
          }
          // not 'contained' within a detachable? pass it on
          for (int i=0; i<mListeners.length; i++)
            mListeners[i].mouseClicked (e);
        }
        public void mouseExited (MouseEvent e) {
          if (last != null) {
            last.setPressed (false);
            last.repaint ();
          }
          last = null;
          // no filtering
          for (int i=0; i<mListeners.length; i++)
            mListeners[i].mouseExited (e);
        }
        public void mouseEntered (MouseEvent e) {
          // no filtering
          for (int i=0; i<mListeners.length; i++)
            mListeners[i].mouseEntered (e);
        }
        public void mousePressed (MouseEvent e) {
          // filter from the other handlers so it doesn't 'change tabs'
          if (getDetachable (e) != null)
            return;
          // not 'contained' within a detachable? pass it on
          for (int i=0; i<mListeners.length; i++)
            mListeners[i].mousePressed (e);
        }
        public void mouseReleased (MouseEvent e) {
          // no filtering
          for (int i=0; i<mListeners.length; i++)
            mListeners[i].mouseReleased (e);
        }
      };
    addMouseListener (ma);
    addMouseMotionListener (ma);
  }

  public void setDetachedIconImage (Image image) {
    detachedIconImage = image;
    Iterator iter = panelToDetMap.values ().iterator ();
    while (iter.hasNext ()) {
      Detachable d = (Detachable) iter.next();
      d.getFrame ().setIconImage (detachedIconImage);
    }
  }

  public Image getDetachedIconImage () {
    return detachedIconImage;
  }

  /**
   * Returns the default Detachable.
   */
  protected Detachable createDetachable (String title, Icon icon,
                                         Component comp,
                                         String tip, int index) {
    return new Detachable (title, icon, comp, tip, index);
  }

  /**
   * Lookup the Detachable for the specified component, which must have been
   * added as a tab. Returns null if not already added.
   */
  protected Detachable getDetachable(Component comp) {
    return (Detachable) panelToDetMap.get(comp);
  }

  /**
   * Return Detachables which have been added as Tabs or Detached Frames. TODO:
   * Currently, order is not accurate.
   */
  protected Detachable[] getDetachables() {
    return (Detachable[]) panelToDetMap.values().toArray(new Detachable[0]);
  }
  
  /**
   * Overridden to add our 'detach' icon. All the <code>add</code> and
   * <code>addTab</code> methods are cover methods for <code>insertTab</code>.
   */
  public void insertTab (String title, Icon icon, Component comp,
                         String tip, int index) {
    // the index we get is based on the number of tabs show, not the number of
    // components, so to remain consistent create the Detachable with an index
    // based on the number of detachables we have
    Detachable d = createDetachable (title, icon, comp, tip,
                                     panelToDetMap.size());
    d.getFrame ().setIconImage (detachedIconImage);
    
    shiftDetachables (true, d);
    panelToDetMap.put (comp, d);

    if (detachable && d.isDetached ())
      detach (d);
    else
      attach (d);
  }
  
  /**
   * Overridden to remove the comopnent from the possible list of components
   * this pane displays
   */
  public void remove (int index) {
    remove ((Detachable)panelToDetMap.get (getComponentAt (index)));
  }
  public void remove (Component comp) {
    Detachable detachable = (Detachable)panelToDetMap.get (comp);
    if (detachable != null)
      remove (detachable);
    else
      super.remove(comp);
  }
  private void remove (Detachable d) {
    if (d != null) {
      panelToDetMap.remove (d.component);
      shiftDetachables (false, d);
      super.remove (d.component);  // ok even if not 'attached'
      d.dispose ();
    }
  }
  /**
   * This keeps the order of the detachables correct when adding or replacing
   * in the tabbed.
   */
  private void shiftDetachables (boolean insert, Detachable cause) {
    Iterator iter = panelToDetMap.values ().iterator ();
    while (iter.hasNext ()) {
      Detachable d = (Detachable) iter.next();
      if (d.index >= cause.index)
        d.index += (insert ? 1 : -1);
    }
  }

  /**
   * Bypass remove and add internal panel to the tabbedpane
   */
  private void detach (Detachable d) {
    if (detachable) {
      super.remove (d.component); // ok, even if not 'attached' yet
      validate ();
      d.setDetached (true);
    }
  }
  
  /**
   * Bypass insertTab and add internal panel to the tabbedpane
   */
  private void attach (Detachable d) {
    int ti;
    for (ti=0; ti<getTabCount(); ti++) {
      Detachable tabD = (Detachable)panelToDetMap.get (getComponentAt(ti));
      if (tabD.index > d.index)
        break;
    }
    d.setDetached (false);
    super.insertTab (d.title, d.icon, d.component, d.tip, ti);
    validate ();
  }

  /**
   * Overridden to hide or show the detached tabs as well. State is retained,
   * so that if you hide this TabbedPane, the detached panels will be hidden,
   * but when you re-show this TabbedPane, the detached panels will be
   * re-shown in their last positions.
   */
  public void setVisible (boolean show) {
    Iterator iter = panelToDetMap.values ().iterator ();
    while (iter.hasNext ()) {
      Detachable d = (Detachable) iter.next();
      if (d.isDetached ())
        d.getFrame().setVisible (show);
    }
  }
  
  public void setDetachable (boolean detachable) {
    if (detachable == this.detachable)
      return;

    this.detachable = detachable;
    //TODO: finish!
    if (detachable) {
      /*
        for each tab, set icon d.icon;
       */
    } else { // !detachable
      /*
        for each detachable, close if open:
        for each tab, set icon d.userIcon;
       */      
    }
  }

  /**
   * Icon which remembers where it was drawn last so that it can be queried
   * with 'contains' requests. Needed because JTabbedPane won't let us put a
   * component (like a button) in the tab itself. This ability will be
   * included in a future Java release, but for now, I've got to do it by hand
   * with this.
   */
  private class LocatedIcon implements Icon {
    Icon icon;
    int x, y;
    LocatedIcon (Icon icon) {
      this.icon = icon;
    }
    boolean contains (int cx, int cy) {
      return (x <= cx) && (cx <= x+icon.getIconWidth()) &&
        (y <= cy) && (cy <= y+icon.getIconHeight());
    }
    public int getIconHeight () {
      return icon.getIconHeight();
    }
    public int getIconWidth () {
      return icon.getIconWidth();
    }
    public void paintIcon (Component c, Graphics g, int px, int py) {
      x = px;
      y = py;
      icon.paintIcon (c, g, x, y);
    }
  }

  /**
   * Class to maintain info for panels as they are added and removed, detached
   * and attached from the <code>DetachableTabPane</code>.
   */
  public class Detachable {

    protected String title = null; // remember to reattach to tabbed pane
    protected Icon icon = null; // possibly composite icon
    protected Icon userIcon = null; // user supplied icon
    protected Component component = null; // component to display
    protected String tip = null;
    protected int index = 0;

    transient Icon cachedIcon = null;
    
    //DetachButton button = null; // displayed in tab for detaching
    protected LocatedIcon button = null; // displayed in tab for detaching
    protected JFrame frame = null; // detached container
    protected boolean detached = false; // keeps detached state if not visible
    
    public Detachable (String title, Icon icon,
                       Component comp, String tip, int index) {

      this.title = title;
      this.userIcon = icon;
      this.component = comp;
      this.tip = tip;
      this.index = index;

      /* frame to display component when detached. */
      this.frame = new JFrame (title!=null?title:comp.getName());
      frame.addWindowListener (new WindowAdapter () {
          public void windowClosing (WindowEvent e) {
            DetachableTabbedPane.this.attach (Detachable.this);
          }
        });

      // put it in the frame to set frames initial sizing
      frame.getContentPane ().add (component);
      frame.validate ();
      frame.pack ();
      frame.getContentPane ().remove (component);  
      // initially attached (added to tabbedPane at creation, so hide)
      frame.setVisible (false);

      button = new LocatedIcon (plainIcon);
      // create composite if neccissary
      if (userIcon != null)
        this.icon = new CompositeIcon (button, userIcon);
      else
        this.icon = button;
    }

    public JFrame getFrame () {
      return frame;
    }

    public Component getComponent () {
      return component;
    }

    public String getTitle () {
      return title;
    }

    public void setPressed (boolean pressed) {
      if (pressed)
        button.icon = pressedIcon;
      else
        button.icon = plainIcon;
      repaint ();
    }
    
    public void repaint () {
      DetachableTabbedPane.this.
        repaint (0, button.x, button.y,
                 button.getIconWidth(),button.getIconHeight());
    }

    public boolean isDetached () {
      return detached;
    }
    
    public void setDetached (boolean detached) {
      this.detached = detached;
      
      if (detached && /*tabbedPane*/ isVisible () && ! frame.isVisible ()) {
        // removeTabAt doesn't even set it visible again in java 1.3, so do it
        // by hand
        component.setVisible (true);
        frame.getContentPane().add (component, BorderLayout.CENTER);
        
        // some window managers like to reposition windows. Don't let 'em
        Rectangle bounds = frame.getBounds();
        
        // don't pack again, so it remains the size the user chose before
        frame.setVisible (true);
        frame.setBounds (bounds);
        frame.validate ();
        
      } else if (! detached && frame.isVisible()) {
        frame.setVisible (false);
        frame.getContentPane().removeAll ();
      } 
    }

    public void dispose () {
      frame.dispose ();
    }

    public boolean contains (int x, int y) {
      return (! detached && button.contains (x, y));
    }
  }
  
  /** Testing */
  public static void main(String s[]) {
    JFrame frame = new JFrame("Annotation Editor Panel Demo");

    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {System.exit(0);}
      });

    DetachableTabbedPane aep = new DetachableTabbedPane ();
        
    // add some tabs
    aep.add ("One", new JLabel ("One"));
    aep.add ("Two", new JLabel ("Two"));

    frame.getContentPane().add(aep);
    frame.pack();
    frame.setVisible(true);
  }

}

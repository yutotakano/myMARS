package mars.venus.editors.jeditsyntax;

import mars.venus.editors.jeditsyntax.tokenmarker.*;
import mars.venus.editors.MARSTextEditingArea;
import mars.venus.EditPane;
import mars.*;

import java.awt.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.*;


/**
 * Adaptor subclass for JEditTextArea
 * <p>
 * Provides those methods required by the MARSTextEditingArea interface
 * that are not defined by JEditTextArea.  This permits JEditTextArea
 * to be used within MARS largely without modification.  DPS 4-20-2010
 *
 * @author Pete Sanderson
 * @since 4.0
 */

public class JEditBasedTextArea extends JEditTextArea implements MARSTextEditingArea, CaretListener {

    private final EditPane editPane;
    private final UndoManager undoManager;
    private boolean isCompoundEdit = false;
    private CompoundEdit compoundEdit;
    private final JEditBasedTextArea sourceCode;


    public JEditBasedTextArea(EditPane editPain, JComponent lineNumbers) {
        super(lineNumbers);
        this.editPane = editPain;
        this.undoManager = new UndoManager();
        this.compoundEdit = new CompoundEdit();
        this.sourceCode = this;

        // Needed to support unlimited undo/redo capability
        UndoableEditListener undoableEditListener = e -> {
            //Remember the edit and update the menus.
            if (isCompoundEdit) {
                compoundEdit.addEdit(e.getEdit());
            } else {
                undoManager.addEdit(e.getEdit());
                editPane.updateUndoState();
                editPane.updateRedoState();
            }
        };
        this.getDocument().addUndoableEditListener(undoableEditListener);
        this.setFont(Globals.getSettings().getEditorFont());
        this.setTokenMarker(new MIPSTokenMarker());

        addCaretListener(this);
    }


    public void setFont(Font f) {
        getPainter().setFont(f);
    }


    public Font getFont() {
        return getPainter().getFont();
    }

    public void setBackground(Color color) {
        getPainter().setBackground(color);
    }

    public Color getBackground() {
        return getPainter().getBackground();
    }

    public void setForeground(Color color) {
        getPainter().setForeground(color);
    }

    public Color getForeground() {
        return getPainter().getForeground();
    }


// 		public void repaint() {		 getPainter().repaint();		 }
// 		 public Dimension getSize() { return painter.getSize(); }
// 		 public void setSize(Dimension d) { painter.setSize(d);}


    /**
     * Use for highlighting the line currently being edited.
     *
     * @param highlight true to enable line highlighting, false to disable.
     */
    public void setLineHighlightEnabled(boolean highlight) {
        getPainter().setLineHighlightEnabled(highlight);
    }

    /**
     * Set the caret blinking rate in milliseconds.  If rate is 0
     * will disable blinking.  If negative, do nothing.
     *
     * @param rate blinking rate in milliseconds
     */
    public void setCaretBlinkRate(int rate) {
        if (rate == 0) {
            caretBlinks = false;
        }
        if (rate > 0) {
            caretBlinks = true;
            caretBlinkRate = rate;
            caretTimer.setDelay(rate);
            caretTimer.setInitialDelay(rate);
            caretTimer.restart();
        }
    }


    /**
     * Set the number of characters a tab will expand to.
     *
     * @param chars number of characters
     */
    public void setTabSize(int chars) {
        painter.setTabSize(chars);
    }

    /**
     * Update the syntax style table, which is obtained from
     * SyntaxUtilities.
     */
    public void updateSyntaxStyles() {
        painter.setStyles(SyntaxUtilities.getCurrentSyntaxStyles());
    }


    public Component getOuterComponent() {
        return this;
    }

    /**
     * Get rid of any accumulated undoable edits.  It is useful to call
     * this method after opening a file into the text area.  The
     * act of setting its text content upon reading the file will generate
     * an undoable edit.  Normally you don't want a freshly-opened file
     * to appear with its Undo action enabled.  But it will unless you
     * call this after setting the text.
     */
    public void discardAllUndoableEdits() {
        this.undoManager.discardAllEdits();
    }

    /**
     * Display caret position on the edit pane.
     *
     * @param e A CaretEvent
     */

    public void caretUpdate(CaretEvent e) {
        editPane.displayCaretPosition(e.getDot());
    }


    /**
     * Same as setSelectedText but named for compatibility with
     * JTextComponent method replaceSelection.
     * DPS, 14 Apr 2010
     *
     * @param replacementText The replacement text for the selection
     */
    public void replaceSelection(String replacementText) {
        setSelectedText(replacementText);
    }

    //
    //
    public void setSelectionVisible(boolean vis) {

    }

    //
    //
    public void setSourceCode(String s, boolean editable) {
        this.setText(s);
        this.setBackground((editable) ? new Color(0x3b3f41) : new Color(0x515353));
        this.setEditable(editable);
        this.setEnabled(editable);
        //this.getCaret().setVisible(editable);
        this.setCaretPosition(0);
        if (editable) this.requestFocusInWindow();
    }

    /**
     * Returns the undo manager for this editing area
     *
     * @return the undo manager
     */
    public UndoManager getUndoManager() {
        return undoManager;
    }

    /**
     * Undo previous edit
     */
    public void undo() {
        // "unredoing" is mode used by DocumentHandler's insertUpdate() and removeUpdate()
        // to pleasingly mark the text and location of the undo.
        unredoing = true;
        try {
            this.undoManager.undo();
        } catch (CannotUndoException ex) {
            System.out.println("Unable to undo: " + ex);
            ex.printStackTrace();
        }
        unredoing = false;
        this.setCaretVisible(true);
    }

    /**
     * Redo previous edit
     */
    public void redo() {
        // "unredoing" is mode used by DocumentHandler's insertUpdate() and removeUpdate()
        // to pleasingly mark the text and location of the redo.
        unredoing = true;
        try {
            this.undoManager.redo();
        } catch (CannotRedoException ex) {
            System.out.println("Unable to redo: " + ex);
            ex.printStackTrace();
        }
        unredoing = false;
        this.setCaretVisible(true);
    }


    //////////////////////////////////////////////////////////////////////////
    //  Methods to support Find/Replace feature
    //
    // Basis for this Find/Replace solution is:
    // http://java.ittoolbox.com/groups/technical-functional/java-l/search-and-replace-using-jtextpane-630964
    // as written by Chris Dickenson in 2005
    //


    /**
     * Finds next occurrence of text in a forward search of a string. Search begins
     * at the current cursor location, and wraps around when the end of the string
     * is reached.
     *
     * @param find          the text to locate in the string
     * @param caseSensitive true if search is to be case-sensitive, false otherwise
     * @return TEXT_FOUND or TEXT_NOT_FOUND, depending on the result.
     */
    public int doFindText(String find, boolean caseSensitive) {
        int findPosn = sourceCode.getCaretPosition();
        int nextPosn;
        nextPosn = nextIndex(sourceCode.getText(), find, findPosn, caseSensitive);
        if (nextPosn >= 0) {
            sourceCode.requestFocus(); // guarantees visibility of the blue highlight
            sourceCode.setSelectionStart(nextPosn); // position cursor at word start
            sourceCode.setSelectionEnd(nextPosn + find.length());
            // Need to repeat start due to quirk in JEditTextArea implementation of setSelectionStart.
            sourceCode.setSelectionStart(nextPosn);
            return TEXT_FOUND;
        } else {
            return TEXT_NOT_FOUND;
        }
    }

    /**
     * Returns next posn of word in text - forward search.  If end of string is
     * reached during the search, will wrap around to the beginning one time.
     *
     * @param input         the string to search
     * @param find          the string to find
     * @param start         the character position to start the search
     * @param caseSensitive true for case sensitive. false to ignore case
     * @return next indexed position of found text or -1 if not found
     */
    private int nextIndex(String input, String find, int start, boolean caseSensitive) {
        int textPosn = -1;
        if (input != null && find != null && start < input.length()) {
            if (caseSensitive) { // indexOf() returns -1 if not found
                textPosn = input.indexOf(find, start);
                // If not found from non-starting cursor position, wrap around
                if (start > 0 && textPosn < 0) {
                    textPosn = input.indexOf(find);
                }
            } else {
                String lowerCaseText = input.toLowerCase();
                textPosn = lowerCaseText.indexOf(find.toLowerCase(), start);
                // If not found from non-starting cursor position, wrap around
                if (start > 0 && textPosn < 0) {
                    textPosn = lowerCaseText.indexOf(find.toLowerCase());
                }
            }
        }
        return textPosn;
    }


    /**
     * Finds and replaces next occurrence of text in a string in a forward search.
     * If cursor is initially at end
     * of matching selection, will immediately replace then find and select the
     * next occurrence if any.  Otherwise it performs a find operation.  The replace
     * can be undone with one undo operation.
     *
     * @param find          the text to locate in the string
     * @param replace       the text to replace the find text with - if the find text exists
     * @param caseSensitive true for case sensitive. false to ignore case
     * @return Returns TEXT_FOUND if not initially at end of selected match and matching
     * occurrence is found.  Returns TEXT_NOT_FOUND if the text is not matched.
     * Returns TEXT_REPLACED_NOT_FOUND_NEXT if replacement is successful but there are
     * no additional matches.  Returns TEXT_REPLACED_FOUND_NEXT if reaplacement is
     * successful and there is at least one additional match.
     */
    public int doReplace(String find, String replace, boolean caseSensitive) {
        int nextPosn;
        int posn;
        // Will perform a "find" and return, unless positioned at the end of
        // a selected "find" result.
        if (find == null || !find.equals(sourceCode.getSelectedText()) ||
                sourceCode.getSelectionEnd() != sourceCode.getCaretPosition()) {
            return doFindText(find, caseSensitive);
        }
        // We are positioned at end of selected "find".  Rreplace and find next.
        nextPosn = sourceCode.getSelectionStart();
        sourceCode.grabFocus();
        sourceCode.setSelectionStart(nextPosn); // posn cursor at word start
        sourceCode.setSelectionEnd(nextPosn + find.length()); //select found text
        // Need to repeat start due to quirk in JEditTextArea implementation of setSelectionStart.
        sourceCode.setSelectionStart(nextPosn);
        isCompoundEdit = true;
        compoundEdit = new CompoundEdit();
        sourceCode.replaceSelection(replace);
        compoundEdit.end();
        undoManager.addEdit(compoundEdit);
        editPane.updateUndoState();
        editPane.updateRedoState();
        isCompoundEdit = false;
        sourceCode.setCaretPosition(nextPosn + replace.length());
        if (doFindText(find, caseSensitive) == TEXT_NOT_FOUND) {
            return TEXT_REPLACED_NOT_FOUND_NEXT;
        } else {
            return TEXT_REPLACED_FOUND_NEXT;
        }
    }

    /**
     * Finds and replaces <B>ALL</B> occurrences of text in a string in a forward search.
     * All replacements are bundled into one CompoundEdit, so one Undo operation will
     * undo all of them.
     *
     * @param find          the text to locate in the string
     * @param replace       the text to replace the find text with - if the find text exists
     * @param caseSensitive true for case sensitive. false to ignore case
     * @return the number of occurrences that were matched and replaced.
     */
    public int doReplaceAll(String find, String replace, boolean caseSensitive) {
        int nextPosn = 0;
        int findPosn = 0; // *** begin at start of text
        int replaceCount = 0;
        compoundEdit = null; // new one will be created upon first replacement
        isCompoundEdit = true; // undo manager's action listener needs this
        while (nextPosn >= 0) {
            nextPosn = nextIndex(sourceCode.getText(), find, findPosn, caseSensitive);
            if (nextPosn >= 0) {
                // nextIndex() will wrap around, which causes infinite loop if
                // find string is a substring of replacement string.  This
                // statement will prevent that.
                if (nextPosn < findPosn) {
                    break;
                }
                sourceCode.grabFocus();
                sourceCode.setSelectionStart(nextPosn); // posn cursor at word start
                sourceCode.setSelectionEnd(nextPosn + find.length()); //select found text
                // Need to repeat start due to quirk in JEditTextArea implementation of setSelectionStart.
                sourceCode.setSelectionStart(nextPosn);
                if (compoundEdit == null) {
                    compoundEdit = new CompoundEdit();
                }
                sourceCode.replaceSelection(replace);
                findPosn = nextPosn + replace.length(); // set for next search
                replaceCount++;
            }
        }
        isCompoundEdit = false;
        // Will be true if any replacements were performed
        if (compoundEdit != null) {
            compoundEdit.end();
            undoManager.addEdit(compoundEdit);
            editPane.updateUndoState();
            editPane.updateRedoState();
        }
        return replaceCount;
    }
    //
    /////////////////////////////  End Find/Replace methods //////////////////////////

    //
    //////////////////////////////////////////////////////////////////

    @Override
	  public void updateColors() { painter.updateColors(); }

}

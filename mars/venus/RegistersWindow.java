package mars.venus;

import mars.*;
import mars.util.*;
import mars.simulator.*;
import mars.mips.hardware.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.table.*;
import javax.swing.event.*;

/*
Copyright (c) 2003-2009,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

/**
 * Sets up a window to display registers in the UI.
 *
 * @author Sanderson, Bumgarner
 **/

public class RegistersWindow extends JPanel implements Observer {
    private static JTable table;
    private static Register[] registers;
    private boolean highlighting;
    private int highlightRow;
    private ExecutePane executePane;
    private static final int NAME_COLUMN = 0;
    private static final int NUMBER_COLUMN = 1;
    private static final int VALUE_COLUMN = 2;
    private static final int UTF16_COLUMN = 3;
    private static final int NOTE_COLUMN = 4;

    private static final int NAME_COLUMN_WIDTH = 50;
    private static final int NUMBER_COLUMN_WIDTH = 40;

    private static final int VALUE_COLUMN_WIDTH = 80;
    private static final int UTF16_COLUMN_WIDTH = 60;

    private static final int NOTE_COLUMN_WIDTH = 30;

    private static Settings settings;

    /**
     * Constructor which sets up a fresh window with a table that contains the register values.
     **/

    public RegistersWindow() {
        Simulator.getInstance().addObserver(this);
        settings = Globals.getSettings();
        this.highlighting = false;
        table = new MyTippedJTable(new RegTableModel(setupWindow()));

        // Initialise cell renderers
        // These display register values (String-ified) right-justified in mono font (except for "note", which doesn't use monospace)
        RegisterCellRenderer cellRendererMonoLeft = new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.LEFT);
        RegisterCellRenderer cellRendererMonoRight = new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.RIGHT);
        RegisterCellRenderer cellRendererRegularLeft = new RegisterCellRenderer(null, SwingConstants.LEFT);
        //UTF runs on cell render central
        RegisterCellRenderer cellRendererCentral = new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT,SwingConstants.CENTER);

        TableColumn col; // current column being edited

        col = table.getColumnModel().getColumn(NAME_COLUMN);
        col.setPreferredWidth(NAME_COLUMN_WIDTH);
        col.setWidth(NAME_COLUMN_WIDTH);
        col.setMaxWidth(NAME_COLUMN_WIDTH);
        col.setCellRenderer(cellRendererMonoLeft);

        col = table.getColumnModel().getColumn(NUMBER_COLUMN);
        col.setPreferredWidth(NUMBER_COLUMN_WIDTH);
        col.setWidth(NUMBER_COLUMN_WIDTH);
        col.setMaxWidth(NUMBER_COLUMN_WIDTH);
        col.setCellRenderer(cellRendererMonoRight);

        col = table.getColumnModel().getColumn(VALUE_COLUMN);
        col.setPreferredWidth(VALUE_COLUMN_WIDTH);
        col.setWidth(VALUE_COLUMN_WIDTH);
        col.setMaxWidth(VALUE_COLUMN_WIDTH);
        col.setCellRenderer(cellRendererMonoRight);

        col = table.getColumnModel().getColumn(UTF16_COLUMN);
        col.setPreferredWidth(UTF16_COLUMN_WIDTH);
        col.setWidth(UTF16_COLUMN_WIDTH);
        col.setMaxWidth(UTF16_COLUMN_WIDTH);
        col.setCellRenderer(cellRendererCentral);

        col = table.getColumnModel().getColumn(NOTE_COLUMN);
        col.setPreferredWidth(NOTE_COLUMN_WIDTH);
        col.setWidth(NOTE_COLUMN_WIDTH);
        col.setCellRenderer(cellRendererRegularLeft);

        table.setPreferredScrollableViewportSize(new Dimension(200, 700));
        this.setLayout(new BorderLayout()); // table display will occupy entire width if widened
        this.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    }

    /**
     * Sets up the data for the window.
     *
     * @return The array object with the data for the window.
     **/

    private Object[][] setupWindow() {
        Object[][] tableData = new Object[35][5];
        registers = RegisterFile.getRegisters();
        for (int i = 0; i < registers.length; i++) {
            tableData[i][0] = registers[i].getName();
            tableData[i][1] = registers[i].getNumber();
            tableData[i][2] = settings.getNumberBaseSetting().formatNumber(registers[i].getValue());
            tableData[i][3] = Character.toString((char) registers[i].getValue());
            tableData[i][4] = "";
        }
        tableData[32][0] = "pc";
        tableData[32][1] = "";//new Integer(32);
        tableData[32][2] = settings.getNumberBaseSetting().formatUnsignedInteger(RegisterFile.getProgramCounter());
        tableData[32][3] = "?";
        tableData[32][4] = "program counter";

        tableData[33][0] = "hi";
        tableData[33][1] = "";//new Integer(33);
        tableData[33][2] = settings.getNumberBaseSetting().formatUnsignedInteger(RegisterFile.getValue(33));
        tableData[33][3] = "?";
        tableData[33][4] = "";

        tableData[34][0] = "lo";
        tableData[34][1] = "";//new Integer(34);
        tableData[34][2] = settings.getNumberBaseSetting().formatUnsignedInteger(RegisterFile.getValue(34));
        tableData[34][3] = "?";
        tableData[34][4] = "";

        return tableData;
    }

    /**
     * clear and redisplay registers
     */
    public void clearWindow() {
        this.clearHighlighting();
        RegisterFile.resetRegisters();
        this.updateRegisters();
    }

    /**
     * Clear highlight background color from any cell currently highlighted.
     */
    public void clearHighlighting() {
        highlighting = false;
        if (table != null) {
            table.tableChanged(new TableModelEvent(table.getModel()));
        }
        highlightRow = -1; // assure highlight will not occur upon re-assemble.
    }

    /**
     * Refresh the table, triggering re-rendering.
     */
    public void refresh() {
        if (table != null) {
            table.tableChanged(new TableModelEvent(table.getModel()));
        }
    }

    /**
     * update register display using specified number base (10 or 16)
     *
     * @param base desired number base
     */
    public void updateRegisters() {
        registers = RegisterFile.getRegisters();

        for (Register register : registers) {
            updateRegisterValue(register.getNumber(), register.getValue());
        }

        updateRegisterUnsignedValue(RegisterFile.getProgramCounter());
        updateRegisterValue(33, RegisterFile.getValue(33));
        updateRegisterValue(34, RegisterFile.getValue(34));
    }

    /**
     * This method handles the updating of the GUI.
     *
     * @param number The number of the register to update.
     * @param val    New value.
     **/

    private void updateRegisterValue(int number, int val) {
        ((RegTableModel) table.getModel()).setDisplayAndModelValueAt(
                settings.getNumberBaseSetting().formatNumber(val), number, VALUE_COLUMN);

        ((RegTableModel) table.getModel()).setDisplayAndModelValueAt(Character.toString((char) val), number, ASCII_COLUMN);
    }


    private void updateRegisterUnsignedValue(int val) {
        ((RegTableModel) table.getModel()).setDisplayAndModelValueAt(
                settings.getNumberBaseSetting().formatUnsignedInteger(val), 32, VALUE_COLUMN);
    }

    /**
     * Required by Observer interface.  Called when notified by an Observable that we are registered with.
     * Observables include:
     * The Simulator object, which lets us know when it starts and stops running
     * A register object, which lets us know of register operations
     * The Simulator keeps us informed of when simulated MIPS execution is active.
     * This is the only time we care about register operations.
     *
     * @param observable The Observable object who is notifying us
     * @param obj        Auxiliary object with additional information.
     */
    public void update(Observable observable, Object obj) {
        if (observable == mars.simulator.Simulator.getInstance()) {
            SimulatorNotice notice = (SimulatorNotice) obj;
            if (notice.getAction() == SimulatorNotice.SIMULATOR_START) {
                // Simulated MIPS execution starts.  Respond to memory changes if running in timed
                // or stepped mode.
                if (notice.getRunSpeed() != RunSpeedPanel.UNLIMITED_SPEED || notice.getMaxSteps() == 1) {
                    RegisterFile.addRegistersObserver(this);
                    this.highlighting = true;
                }
            } else {
                // Simulated MIPS execution stops.  Stop responding.
                RegisterFile.deleteRegistersObserver(this);
            }
        } else if (obj instanceof RegisterAccessNotice) {
            // NOTE: each register is a separate Observable
            RegisterAccessNotice access = (RegisterAccessNotice) obj;
            if (access.getAccessType() == AccessNotice.WRITE) {
                // Uses the same highlighting technique as for Text Segment -- see
                // AddressCellRenderer class in DataSegmentWindow.java.
                this.highlighting = true;
                this.highlightCellForRegister((Register) observable);
                Globals.getGui().getRegistersPane().setSelectedComponent(this);
            }
        }
    }

    /**
     * Highlight the row corresponding to the given register.
     *
     * @param register Register object corresponding to row to be selected.
     */
    private void highlightCellForRegister(Register register) {
        this.highlightRow = register.getNumber();
        // Tell the system that table contents have changed.  This will trigger re-rendering
        // during which cell renderers are obtained.  The row of interest (identified by
        // instance variabls this.registerRow) will get a renderer
        // with highlight background color and all others get renderer with default background.
        table.tableChanged(new TableModelEvent(table.getModel()));
    }

    /*
    * Cell renderer for displaying register entries.  This does highlighting, so if you
    * don't want highlighting for a given column, don't use this.  Currently we highlight
    * all columns.
    */
    private class RegisterCellRenderer extends DefaultTableCellRenderer {
        private Font font;
        private final int alignment;

        RegisterCellRenderer(Font font, int alignment) {
            super();
            if (font != null) {
                this.font = font;
            }
            this.alignment = alignment;
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            cell.setFont(font);
            cell.setHorizontalAlignment(alignment);

            if (BooleanSetting.REGISTERS_HIGHLIGHTING.get() && highlighting && row == highlightRow) {
                cell.setBackground(settings.getColorSettingByPosition(Settings.REGISTER_HIGHLIGHT_BACKGROUND));
                cell.setForeground(settings.getColorSettingByPosition(Settings.REGISTER_HIGHLIGHT_FOREGROUND));
                cell.setFont(settings.getFontByPosition(Settings.REGISTER_HIGHLIGHT_FONT));
            } else if (row % 2 == 0) {
                cell.setBackground(settings.getColorSettingByPosition(Settings.EVEN_ROW_BACKGROUND));
                cell.setForeground(settings.getColorSettingByPosition(Settings.EVEN_ROW_FOREGROUND));
                cell.setFont(settings.getFontByPosition(Settings.EVEN_ROW_FONT));
            } else {
                cell.setBackground(settings.getColorSettingByPosition(Settings.ODD_ROW_BACKGROUND));
                cell.setForeground(settings.getColorSettingByPosition(Settings.ODD_ROW_FOREGROUND));
                cell.setFont(settings.getFontByPosition(Settings.ODD_ROW_FONT));
            }
            return cell;
        }
    }


    ////////////////////////////////////////////////////////////////////////////

    class RegTableModel extends AbstractTableModel {
        final String[] columnNames = {"Name", "Num", "Value", "UTF-16", "Note"};
        final Object[][] data;

        RegTableModel(Object[][] d) {
            data = d;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.
      	*/
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
   * Don't need to implement this method unless your table's
   * editable.
   */
        public boolean isCellEditable(int row, int col) {
            if (col == NOTE_COLUMN) {
                return true;
            }

            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            // these registers are not editable: $zero (0), $pc (32), $ra (31)
            return col == VALUE_COLUMN && row != 0 && row != 32 && row != 31;
        }


        /*
         * Update cell contents in table model.  This method should be called
      	* only when user edits cell, so input validation has to be done.  If
      	* value is valid, MIPS register is updated.
         */
        public void setValueAt(Object value, int row, int col) {
            if (col == NOTE_COLUMN) {
                data[row][col] = value;
                fireTableCellUpdated(row, col);
                return;
            }
            int val;
            try {
                val = Binary.stringToInt((String) value);
            } catch (NumberFormatException nfe) {
                data[row][col] = "INVALID";
                fireTableCellUpdated(row, col);
                return;
            }
            //  Assures that if changed during MIPS program execution, the update will
            //  occur only between MIPS instructions.
            synchronized (Globals.memoryAndRegistersLock) {
                RegisterFile.updateRegister(row, val);
            }

            data[row][col] = Globals.getSettings().getNumberBaseSetting().formatNumber(val);
            fireTableCellUpdated(row, col);
        }


        /**
         * Update cell contents in table model.  Does not affect MIPS register.
         */
        private void setDisplayAndModelValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }


        // handy for debugging....
        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i = 0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j = 0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }

    ///////////////////////////////////////////////////////////////////
    //
    // JTable subclass to provide custom tool tips for each of the
    // register table column headers and for each register name in
    // the first column. From Sun's JTable tutorial.
    // http://java.sun.com/docs/books/tutorial/uiswing/components/table.html
    //
    private class MyTippedJTable extends JTable {
        MyTippedJTable(RegTableModel m) {
            super(m);
            this.setRowSelectionAllowed(true); // highlights background color of entire row
            this.setSelectionBackground(Color.GREEN);
        }

        private final String[] regToolTips = {
            /* $zero */  "constant 0",
            /* $at   */  "reserved for assembler",
            /* $v0   */  "expression evaluation and results of a function",
            /* $v1   */  "expression evaluation and results of a function",
            /* $a0   */  "argument 1",
            /* $a1   */  "argument 2",
            /* $a2   */  "argument 3",
            /* $a3   */  "argument 4",
            /* $t0   */  "temporary (not preserved across call)",
            /* $t1   */  "temporary (not preserved across call)",
            /* $t2   */  "temporary (not preserved across call)",
            /* $t3   */  "temporary (not preserved across call)",
            /* $t4   */  "temporary (not preserved across call)",
            /* $t5   */  "temporary (not preserved across call)",
            /* $t6   */  "temporary (not preserved across call)",
            /* $t7   */  "temporary (not preserved across call)",
            /* $s0   */  "saved temporary (preserved across call)",
            /* $s1   */  "saved temporary (preserved across call)",
            /* $s2   */  "saved temporary (preserved across call)",
            /* $s3   */  "saved temporary (preserved across call)",
            /* $s4   */  "saved temporary (preserved across call)",
            /* $s5   */  "saved temporary (preserved across call)",
            /* $s6   */  "saved temporary (preserved across call)",
            /* $s7   */  "saved temporary (preserved across call)",
            /* $t8   */  "temporary (not preserved across call)",
            /* $t9   */  "temporary (not preserved across call)",
            /* $k0   */  "reserved for OS kernel",
            /* $k1   */  "reserved for OS kernel",
            /* $gp   */  "pointer to global area",
            /* $sp   */  "stack pointer",
            /* $fp   */  "frame pointer",
            /* $ra   */  "return address (used by function call)",
            /* pc    */  "program counter",
            /* hi    */  "high-order word of multiply product, or divide remainder",
            /* lo    */  "low-order word of multiply product, or divide quotient"
        };

        //Implement table cell tool tips.
        public String getToolTipText(MouseEvent e) {
            String tip;
            java.awt.Point p = e.getPoint();
            int rowIndex = rowAtPoint(p);
            int colIndex = columnAtPoint(p);
            int realColumnIndex = convertColumnIndexToModel(colIndex);
            if (realColumnIndex == NAME_COLUMN) { //Register name column
                tip = regToolTips[rowIndex];
            /* You can customize each tip to encorporiate cell contents if you like:
               TableModel model = getModel();
               String regName = (String)model.getValueAt(rowIndex,0);
            	....... etc .......
            */
            } else {
                //You can omit this part if you know you don't have any
                //renderers that supply their own tool tips.
                tip = super.getToolTipText(e);
            }
            return tip;
        }

        private final String[] columnToolTips = {
                "Each register has a tool tip describing its usage convention", // name
                "Corresponding register number", // register number
                "Current 32 bit value", // value
                "Current value encoded as an UTF-16 Encoded character", // UTF-16 encoded
                "Annotate your registers by double clicking the cell", // annotation
        };

        //Implement table header tool tips.
        protected JTableHeader createDefaultTableHeader() {
            return
                    new JTableHeader(columnModel) {
                        public String getToolTipText(MouseEvent e) {
                            String tip = null;
                            java.awt.Point p = e.getPoint();
                            int index = columnModel.getColumnIndexAtX(p.x);
                            int realIndex = columnModel.getColumn(index).getModelIndex();
                            return columnToolTips[realIndex];
                        }
                    };
        }
    }

}

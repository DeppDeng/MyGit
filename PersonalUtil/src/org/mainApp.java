/**
 * @(#)mainApp.java
 *
 *
 * @author Saint(旋幻圣殿)
 * @version 1.00 2010/1/10
 */
package org;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import saint.swing.*;
import saint.io.*;
import saint.swing.filechooser.*;
import saint.base.ColorFont;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import java.util.Hashtable;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;

public class mainApp extends JFrame {
//*****************窗口区********************//
	private static final int WIDTH = 640;
	private static final int HEIGHT = 480;
	private static final String FrameTitle = "文本编辑器";
	private boolean Running = false;
	private GridBagManager gbm;
	private boolean Saved = true;
	private File EditingFile = null;
	
//*****************菜单区********************//
	private java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	private JFileChooser chooser = new JFileChooser();
	private JCheckBoxMenuItem autoBreak;
	private ColorFont colorFont = null;
	
//*****************编辑区********************//
	private JTextArea editArea;
	private UndoAction undoAction = new UndoAction();
	private RedoAction redoAction = new RedoAction();
	private UndoManager undo = new UndoManager();
	private UndoableEditListener undoHandler = new UndoHandler();
	
//*****************状态栏区********************//
	private StatusPane status;

//*****************事件区********************//
	private Hashtable<String,SntAction> cmdHash;
	private SntAction[] defaultActions = {
		new NewAction(),
		new OpenAction(),
		new SaveAction(),
		new SaveAsAction(),
		new ExitAction(),
		new CutAction(),
		new CopyAction(),
		new PasteAction(),
		new DeleteAction(),
		new FindAction(),
		new FindNextAction(),
		new ReplaceAction(),
		new GotoAction(),
		new SelectAllAction(),
		new DateAction(),
		redoAction,
		undoAction
	};
	
//*****************实例化********************//
    public mainApp() {
    	super(FrameTitle);
    	FrameProperty.setDefaultFont();
    	FrameProperty.DefaultSetting(this, WIDTH, HEIGHT, null, 0);
    	this.setLayout(new GridBagLayout());
    	gbm = new GridBagManager();
    	
    	cmdHash = new Hashtable<String,SntAction>();
    	for(SntAction a:defaultActions) {
    		cmdHash.put((String)a.getValue(Action.NAME), a);
    	}
    	
    	this.setJMenuBar(this.createMenuBar());
    	clipboard.addFlavorListener(new FlavorHandler());
    	getAction("粘贴").update();
    	chooser.setAcceptAllFileFilterUsed(true);
    	chooser.setCurrentDirectory(new File("use.dir"));
    	SeniorFilter.setFileFilter(chooser,".txt",".lrc",".java",".ini");
    	
    	editArea = this.createEditArea();
    	editArea.setComponentPopupMenu(this.createPopupMenu());
    	editArea.getDocument().addUndoableEditListener(undoHandler);
    	editArea.getDocument().addDocumentListener(new EditHandler());
    	editArea.addCaretListener(new CaretHandler());
    	undo.setLimit(20);
    	JScrollPane jsp = new JScrollPane(editArea);
    	gbm.setProperty(null, GridBagConstraints.BOTH, GridBagConstraints.SOUTHEAST);
    	gbm.add(this, jsp, 1,1,1,1,1,1); 
    	
    	status = new StatusPane();
    	gbm.setProperty(null, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
    	gbm.add(this, status, 1,2,1,1,1,0);
    	
    	this.addWindowListener(new java.awt.event.WindowAdapter() {
    		public void windowClosing(java.awt.event.WindowEvent e) {
    			if(!Saved && editArea.getText().length()>0) {
	    			int result = JOptionPane.showConfirmDialog(mainApp.this,"是否保存文件","文件还未保存",JOptionPane.YES_NO_OPTION);
					if(result==JOptionPane.YES_OPTION) {
						if(showSaveOption())
							System.exit(0);
						return;
					}else{
						System.exit(0);	
					}
    			}
    			System.exit(0);
    		}
    	});
    }
    private void addStatus() {
    	gbm.setProperty(null, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
    	gbm.add(this, status, 1,2,1,1,1,0);
    	this.validate();
    }
    private void removeStatus() {
    	this.remove(status);
    	this.validate();
    }
    private String formatString(String str) {
    	String rstr = str;
    	if(str.toLowerCase().endsWith("\r\n")){
    		return rstr;
    	}else if(str.toLowerCase().endsWith("\n")) {
    		rstr = rstr.replace("\n","\r\n");
    		return rstr;
    	}else if(str.toLowerCase().endsWith("\r")){
    		rstr = rstr+"\n";
    		return rstr;
    	}else{
    		rstr = rstr+"\r\n";
    		return rstr;
    	}
    	//return rstr;
    }

//*****************菜单区********************//
    private JMenuBar createMenuBar() {
    	JMenuBar bar = new JMenuBar();
    	
    	JMenu file = new JMenu("文件(F)");
    	file.setMnemonic('F');
    	file.add(createMenuItem("新建"));
    	file.addSeparator();
    	file.add(createMenuItem("打开"));
    	file.add(createMenuItem("保存"));
    	file.add(createMenuItem("另存为"));
    	file.addSeparator();
    	file.add(createMenuItem("退出"));
    	
    	JMenu edit = new JMenu("编辑(E)");
    	edit.setMnemonic('E');
    	edit.add(createMenuItem("撤销"));
    	edit.add(createMenuItem("重做"));
    	edit.addSeparator();
    	edit.add(createMenuItem("剪切"));
    	edit.add(createMenuItem("复制"));
    	edit.add(createMenuItem("粘贴"));
    	edit.add(createMenuItem("删除"));
    	edit.addSeparator();
    	edit.add(createMenuItem("查找"));
    	edit.add(createMenuItem("查找下一个"));
    	edit.add(createMenuItem("替换"));
    	edit.add(createMenuItem("转到"));
    	edit.addSeparator();
    	edit.add(createMenuItem("全选"));
    	edit.add(createMenuItem("时间/日期"));
    	
    	JMenu form = new JMenu("视图(O)");
    	form.setMnemonic('O');
    	autoBreak = new JCheckBoxMenuItem("自动换行(W)",false);
    	autoBreak.setMnemonic('W');
    	autoBreak.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			JCheckBoxMenuItem c = (JCheckBoxMenuItem) e.getSource();
    			if(!c.getState()) {
    				getEditArea().setLineWrap(false);
    				c.setState(false);
    			}else{
    				getEditArea().setLineWrap(true);
    				c.setState(true);
    			}
    		}
    	});
    	form.add(autoBreak);
    	JMenuItem font = new JMenuItem("字体(F)");
    	font.setMnemonic('F');
    	font.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			if(colorFont==null)
    				colorFont = new ColorFont();
    			JFontChooser fntch = new JFontChooser(colorFont.getFont(),colorFont.getColor());
    			int result = fntch.showDialog(null,null);
    			if(result==JFontChooser.APPROVE_OPTION) {
    				colorFont = fntch.getSelectColorFont();
    				getEditArea().setFont(colorFont.getFont());
    				getEditArea().setForeground(colorFont.getColor()); 
    			}
    		}
    	});
    	form.add(font);
    	JMenu skin = new JMenu("皮肤(P)");
    	skin.setMnemonic('P');
    	ButtonGroup group = new ButtonGroup();
    	JRadioButtonMenuItem skin1 = new JRadioButtonMenuItem("经典皮肤");
    	skin1.addActionListener(new UIChangeAction("经典皮肤", 
    		"org.jvnet.substance.skin.SubstanceOfficeBlue2007LookAndFeel"));
    	skin1.setSelected(true);
    	JRadioButtonMenuItem skin2 = new JRadioButtonMenuItem("Autumn");
    	skin2.addActionListener(new UIChangeAction("Autumn", 
    		"org.jvnet.substance.skin.SubstanceAutumnLookAndFeel"));
    	JRadioButtonMenuItem skin3 = new JRadioButtonMenuItem("Creme");
    	skin3.addActionListener(new UIChangeAction("Creme", 
    		"org.jvnet.substance.skin.SubstanceCremeLookAndFeel"));
    	group.add(skin1);
    	skin.add(skin1);
    	group.add(skin2);
    	skin.add(skin2);
    	group.add(skin3);
    	skin.add(skin3);
    	form.add(skin);
    	
    	
    	JMenu view = new JMenu("查看(V)");
    	view.setMnemonic('V');
    	JCheckBoxMenuItem statusb = new JCheckBoxMenuItem("状态栏(S)",true);
    	statusb.setMnemonic('S');
    	statusb.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			JCheckBoxMenuItem c = (JCheckBoxMenuItem) e.getSource();
    			if(!c.getState()) {
    				mainApp.this.removeStatus();
    				c.setState(false);
    			}else{
    				mainApp.this.addStatus();
    				c.setState(true);
    			}
    		}
    	});
    	view.add(statusb);
    	
    	JMenu help = new JMenu("帮助(H)");
    	help.setMnemonic('H');
    	JMenuItem about = new JMenuItem("关于文本编辑器(A)");
    	about.setMnemonic('A');
    	about.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			JOptionPane.showMessageDialog(mainApp.this, 
    				"文本编辑器 (Notepad)\n\n版本号 V1.00 2010/1/10\n\n版权所有 (C) 2010 旋幻圣殿\n\n作者 Saint(旋幻圣殿)", 
    					"关于文本编辑器", JOptionPane.PLAIN_MESSAGE, new ImageIcon("snt\\img\\sntIcon32.png"));
    		}
    	});
    	help.add(about);
    	
    	bar.add(file);
    	bar.add(edit);
    	bar.add(form);
    	bar.add(view);
    	bar.add(help);
    	return bar; 
    }
    
    private JMenuItem createMenuItem(String name) {
    	JMenuItem mi = new JMenuItem();
    	SntAction a = cmdHash.get(name);
    	if(a!=null) {
    		mi.setText(name+"("+a.getMnemonic()+")");
    		mi.setMnemonic(a.getMnemonic());
    		if(a.getAccelerator()!=null)
    			mi.setAccelerator(a.getAccelerator());
    		mi.addActionListener(a);
		    a.addPropertyChangeListener(new PropertyChangeHandler(mi));
		    mi.setEnabled(a.isEnabled());
    	}else{
    		mi.setText(name);
    		mi.setEnabled(false);
    	}
    	return mi;
    }
    private JPopupMenu createPopupMenu() {
    	JPopupMenu pop = new JPopupMenu();
    	
    	pop.add(createMenuItem("撤销"));
    	pop.add(createMenuItem("重做"));
    	pop.addSeparator();
    	pop.add(createMenuItem("剪切"));
    	pop.add(createMenuItem("复制"));
    	pop.add(createMenuItem("粘贴"));
    	pop.add(createMenuItem("删除"));
    	pop.addSeparator();
    	pop.add(createMenuItem("查找"));
    	pop.add(createMenuItem("替换"));
    	pop.add("旋幻圣殿系列软件");
    	
    	return pop;
    }
    private void fireMenuEnabledChange() {
    	for(SntAction a:defaultActions) {
    		a.update();
    	}
    	if(Running) {
			autoBreak.setEnabled(false);
		}else{
			autoBreak.setEnabled(true);
		}
    }
    private boolean showSaveOption() {
    	try {
    		if(EditingFile==null) {
    			int result = chooser.showSaveDialog(null);
    			if(result==JFileChooser.APPROVE_OPTION) {
    				String path = chooser.getSelectedFile().getAbsolutePath();
    				if(!java.util.regex.Pattern.matches("^.+[\\.]{1}[\\w]{3,5}", path))
	    					path = path+".txt";
    				EditingFile = new File(path);
    			}else{
    				return false;
    			}
    		}
    		int line = getEditArea().getLineCount();
    		if(line<1000) {
    			Running = true;
    			fireMenuEnabledChange();
    			PrintWriter pw = new PrintWriter(new FileWriter(EditingFile));
    			for(int i=0; i<line; i++) {
    				pw.print(formatString(getStringOfLine(i)));
    			}
    			Running = false;
    			fireMenuEnabledChange();
    			Saved = true;
    			status.updateTitle(EditingFile.getName());
    			pw.close();
    		}else{
    			new SaveThread(EditingFile);
    		}
    		return true;
    	}catch(Exception ex) {}	
    	return false;
    }
    private boolean showSaveAsOption() {
    	try {
    		File file;
    		int result = chooser.showSaveDialog(null);
    		if(result==JFileChooser.APPROVE_OPTION) {
    			String path = chooser.getSelectedFile().getAbsolutePath();
    			if(!java.util.regex.Pattern.matches("^.+[\\.]{1}[\\w]{3,5}", path))
	    			path = path+".txt";
    			file = new File(path);
    			EditingFile = file;
    		}else{
    			return false;
    		}
    		int line = getEditArea().getLineCount();
    		if(line<1000) {
    			Running = true;
    			fireMenuEnabledChange();
    			PrintWriter pw = new PrintWriter(new FileWriter(file));
    			for(int i=0; i<line; i++) {
    				pw.print(formatString(getStringOfLine(i)));
    			}
    			Running = false;
    			fireMenuEnabledChange();
    			Saved = true;
    			status.updateTitle(EditingFile.getName());
    			pw.close();
    		}else{
    			new SaveThread(file);
    		}
    		
    		return true;
    	}catch(Exception ex) {}	
    	return false;
    }
    
//*****************编辑区********************//
    protected JTextArea createEditArea() {
    	JTextArea jta = new JTextArea();
    	jta.setDragEnabled(true);
    	return jta;
    }
    protected JTextArea getEditArea() {
    	return editArea;
    }
    private String getStringOfLine(int line) {
    	int offset;
    	int len;
    	try{
    		if(line==0) {
	    		offset = 0;
	    	}else{
	    		offset = getEditArea().getLineEndOffset(line-1);
	    	}
	    	len = getEditArea().getLineEndOffset(line)-offset;
	    	return getEditArea().getText(offset,len);
    	}catch(Exception ex){
    		System.out.println("can not getStringOfLine at EditArea!");
    	}
    	return "";
    }
    
//*****************状态栏区********************//
    class StatusPane extends JPanel {
    	private JLabel title;
		public JProgressBar progressBar;
		private JLabel row;
		private JLabel cell;
    	public StatusPane() {
    		super();
    		this.setPreferredSize(new Dimension(30,20));
    		this.setMaximumSize(new Dimension(30,20));
    		this.setMinimumSize(new Dimension(30,20));
    		this.setLayout(new GridBagLayout());
    		//this.setBorder(BorderFactory.createEtchedBorder(0));
    		
    		title = new JLabel("未命名",2);
	    	GridBagManager gbm = new GridBagManager();
	    	gbm.setProperty(new Insets(0,5,0,0),GridBagConstraints.HORIZONTAL,GridBagConstraints.EAST);
	    	gbm.add(this, title, 1,1,1,1,1,0);
	    	
	    	progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
	    	progressBar.setStringPainted(true);
	    	progressBar.setVisible(false);
	    	progressBar.setPreferredSize(new Dimension(120,17));
	    	progressBar.setMaximumSize(new Dimension(120,17));
	    	progressBar.setMinimumSize(new Dimension(120,17));
	    	gbm.setProperty(new Insets(1,0,0,0),GridBagConstraints.NONE,GridBagConstraints.EAST);
	    	gbm.add(this, progressBar,2,1,1,1,0,0);
	    	
	    	JLabel txt1 = new JLabel("行:");
	    	gbm.setProperty(new Insets(0,16,0,0),GridBagConstraints.NONE,GridBagConstraints.EAST);
	    	gbm.add(this, txt1, 3,1,1,1,0,0);
	    	row = new JLabel("1",2);
	    	row.setPreferredSize(new Dimension(60,18));
	    	row.setMaximumSize(new Dimension(60,18));
	    	row.setMinimumSize(new Dimension(60,18));
	    	gbm.setProperty(new Insets(0,2,0,0),GridBagConstraints.NONE,GridBagConstraints.EAST);
	    	gbm.add(this, row, 4,1,1,1,0,0);
	    	
	    	JLabel txt2 = new JLabel("列:");
	    	gbm.setProperty(new Insets(0,2,0,0),GridBagConstraints.NONE,GridBagConstraints.EAST);
	    	gbm.add(this, txt2, 5,1,1,1,0,0);
	    	cell = new JLabel("0",2);
	    	cell.setPreferredSize(new Dimension(60,18));
	    	cell.setMaximumSize(new Dimension(60,18));
	    	cell.setMinimumSize(new Dimension(60,18));
	    	gbm.setProperty(new Insets(0,2,0,0),GridBagConstraints.NONE,GridBagConstraints.EAST);
	    	gbm.add(this, cell, 6,1,1,1,0,0);
    	}
    	public void updateTitle(String str) {
    		this.title.setText(str);
    	}
    	public void setProgressBarVisible(boolean b) {
    		this.progressBar.setVisible(b);
    	}
    	public void setProgressBarValue(int n) {
    		this.progressBar.setValue(n);
    	}
    	public void updateRowAndCell(int r, int c) {
    		this.row.setText(""+r);
    		this.cell.setText(""+c);
    	}
    }

//*****************事件监听区********************//
    //Undo Listener
    class UndoHandler implements UndoableEditListener {
    	public void undoableEditHappened(UndoableEditEvent e) {
		    undo.addEdit(e.getEdit());
		    undoAction.update();
		    redoAction.update();
    	}
    }
    //光标 属性监听
    class CaretHandler implements CaretListener {
    	public void caretUpdate(CaretEvent e){
			fireMenuEnabledChange();
			try{
				int r = getEditArea().getLineOfOffset(e.getDot());
				int c = e.getDot()-getEditArea().getLineStartOffset(r);
				status.updateRowAndCell(r+1,c);
			}catch(Exception ex){}
		}
    }
    //DocumentListener
    class EditHandler implements DocumentListener {
    	public void changedUpdate(DocumentEvent e) {
	    	Saved = false;
	    }
	    public void insertUpdate(DocumentEvent e) {
	    	Saved = false;
	    }
	    public void removeUpdate(DocumentEvent e) {
	    	Saved = false;
	    }
    }
    //Action 属性监听
    class PropertyChangeHandler implements PropertyChangeListener {
    	JMenuItem menuItem;
    	public PropertyChangeHandler(JMenuItem mi) {
    		super();
    		this.menuItem = mi;
    	}
    	public void propertyChange(PropertyChangeEvent e) {
    		String propertyName = e.getPropertyName();
            if (e.getPropertyName().equals(Action.NAME)) {
                String text = (String) e.getNewValue();
                menuItem.setText(text);
            } else if (propertyName.equals("enabled")) {
                Boolean enabledState = (Boolean) e.getNewValue();
                menuItem.setEnabled(enabledState.booleanValue());
            }
    	}
    }
    //Flavor 监听
    class FlavorHandler implements FlavorListener {
    	public void flavorsChanged(FlavorEvent e){
    		getAction("粘贴").update();
    	}
    }
    
//*****************事件区********************//
	private SntAction getAction(String name) {
		return cmdHash.get(name);
	}
	//基础Action
	class SntAction extends AbstractAction {
		private char Mnemonic;
		private KeyStroke keyStore = null;
		public SntAction() {
			super();
		}
		public SntAction(String name) {
			super(name);
		}
		public SntAction(String name, Icon icon) {
			super(name, icon);
		}
		public void actionPerformed(ActionEvent e) {}
		public char getMnemonic() {
			return Mnemonic;
		}
		public KeyStroke getAccelerator() {
			return keyStore;
		}
		public void setMnemonic(char c) {
			this.Mnemonic = c;
		}
		public void setAccelerator(KeyStroke key) {
			this.keyStore  = key;
		}
		protected void update(){}
	}
	//新建Action
	class NewAction extends SntAction {
		public NewAction() {
			this("新建");
		}
		public NewAction(String name) {
			super(name);
			setEnabled(true);
			setMnemonic('N');
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		}
		public void actionPerformed(ActionEvent e) {
			if(!Saved) {
	    		int result = JOptionPane.showConfirmDialog(mainApp.this,"是否保存文件","文件还未保存",JOptionPane.YES_NO_OPTION);
				if(result==JOptionPane.YES_OPTION) {
					showSaveOption();
				}
    		}
    		getEditArea().setText("");
			undo.discardAllEdits();
			Running = false;
			fireMenuEnabledChange();
			status.updateTitle("未命名");
			EditingFile = null;
		}
		protected void update() {
			if(Running) {
				setEnabled(false);
			}else{
				setEnabled(true);
			}
		}
	}
	//打开Action
	class OpenAction extends SntAction {
		public OpenAction() {
			this("打开");
		}
		public OpenAction(String name) {
			super(name);
			setEnabled(true);
			setMnemonic('O');
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		}
		public void actionPerformed(ActionEvent e) {
			if(!Saved && editArea.getText().length()>0) {
	    		int results = JOptionPane.showConfirmDialog(mainApp.this,"是否保存文件","文件还未保存",JOptionPane.YES_NO_OPTION);
				if(results==JOptionPane.YES_OPTION) {
					showSaveOption();
					return;
				}
    		}
			int result = chooser.showOpenDialog(null);
	    	if(result==JFileChooser.APPROVE_OPTION) {
	    		if(EditingFile!=null) {
	    			if(EditingFile.equals(chooser.getSelectedFile().getAbsoluteFile()))
	    				return;
	    		}
	    		undo.discardAllEdits();
	    		getEditArea().getDocument().removeUndoableEditListener(undoHandler);
	    		EditingFile = chooser.getSelectedFile().getAbsoluteFile();
	    		status.updateTitle(EditingFile.getName());
	    		if(EditingFile.length()/1024>500) {
	    			new OpenThread(EditingFile);
	    		}else{
	    			try{
	    				Running = true;
	    				fireMenuEnabledChange();
		    			LineNumberReader buf = new LineNumberReader(
		    				new InputStreamReader(new FileInputStream(EditingFile)));
		    			getEditArea().setText("");
		    			String tmp;
		    			while( (tmp=buf.readLine())!=null ) {
		    				getEditArea().append(formatString(tmp));
		    			}
	    			}catch(Exception ex) {
	    			}finally{
	    				Running = false;
	    				fireMenuEnabledChange();
	    				getEditArea().getDocument().addUndoableEditListener(undoHandler);
	    				Saved = true;
	    			}
	    		}
	    	}
		}
		protected void update() {
			if(Running) {
				setEnabled(false);
			}else{
				setEnabled(true);
			}
		}
	}
	//保存Action
	class SaveAction extends SntAction {
		public SaveAction() {
			this("保存");
		}
		public SaveAction(String name) {
			super(name);
			setEnabled(true);
			setMnemonic('S');
			setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		}
		public void actionPerformed(ActionEvent e) {
			showSaveOption();
		}
		protected void update() {
			if(Running) {
				setEnabled(false);
			}else{
				setEnabled(true);
			}
		}
	}
	//另存为Action
	class SaveAsAction extends SntAction {
		public SaveAsAction() {
			this("另存为");
		}
		public SaveAsAction(String name) {
			super(name);
			setEnabled(true);
			setMnemonic('A');
		}
		public void actionPerformed(ActionEvent e) {
			showSaveAsOption();
		}
		protected void update() {
			if(Running) {
				setEnabled(false);
			}else{
				setEnabled(true);
			}
		}
	}
	//退出Action
	class ExitAction extends SntAction {
		public ExitAction() {
			this("退出");
		}
		public ExitAction(String name) {
			super(name);
			setEnabled(true);
			setMnemonic('X');
		}
		public void actionPerformed(ActionEvent e) {
			if(!Saved  && editArea.getText().length()>0) {
				int result = JOptionPane.showConfirmDialog(mainApp.this,"是否保存文件","文件还未保存",JOptionPane.YES_NO_OPTION);
				if(result==JOptionPane.YES_OPTION) {
					if(showSaveOption())
						System.exit(0);
					return;
				}else{
					System.exit(0);	
				}
			}else{
				System.exit(0);
			}
		}
		protected void update() {
			
		}
	}
    //Undo Action
    class UndoAction extends SntAction {
    	public UndoAction() {
    		this("撤销");
    	}
    	public UndoAction(String name) {
    		super(name);
    		setEnabled(false);
    		setMnemonic('U');
    		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
    	}
    	public void actionPerformed(ActionEvent e) {
    		try {
    			undo.undo();
    		}catch(Exception ex) {
    			System.out.println("Undo Exception");
    		}
    		update();
    		redoAction.update();
    	}
    	protected void update() {
    		if(undo.canUndo()) {
    			setEnabled(true);
    			putValue(Action.NAME, undo.getUndoPresentationName()+"("+getMnemonic()+")");
    		}else{
    			setEnabled(false);
    			putValue(Action.NAME, "撤销"+"("+getMnemonic()+")");
    		}
    	}
    }
    
    //Redo Action
    class RedoAction extends SntAction {
    	public RedoAction() {
    		this("重做");
    	}
    	public RedoAction(String name) {
    		super(name);
    		setEnabled(false);
    		setMnemonic('O');
    		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
    	}
    	public void actionPerformed(ActionEvent e) {
    		try{
    			undo.redo();
    		}catch(Exception ex) {
    			System.out.println("Redo Exception!");
    		}
    		update();
    		undoAction.update();
    	}
    	protected void update() {
    		if(undo.canRedo()) {
    			setEnabled(true);
    			putValue(Action.NAME, undo.getRedoPresentationName()+"("+getMnemonic()+")");
    		}else{
    			setEnabled(false);
    			putValue(Action.NAME, "重做"+"("+getMnemonic()+")");
    		}
    	}
    }
    //剪切Action
    class CutAction extends SntAction {
    	public CutAction() {
    		this("剪切");
    	}
    	public CutAction(String name) {
    		super(name);
    		setEnabled(false);
    		setMnemonic('T');
    		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
    	}
    	public void actionPerformed(ActionEvent e) {
    		try{
    			getEditArea().cut();
    		}catch(Exception ex) {
    			System.out.println("Cut Exception!");
    		}
    		update();
			getAction("复制").update();
			getAction("删除").update();
    	}
    	protected void update() {
    		String text = null;
    		try{
    			text = getEditArea().getSelectedText();
    		}catch(Exception ex) {
    			System.out.println("Select Exception!");
    		}
    		if(text!=null && !Running) {
    			setEnabled(true);
    		}else{
    			setEnabled(false);
    		}
    	}
    }
    //复制Action
    class CopyAction extends SntAction {
    	public CopyAction() {
    		this("复制");
    	}
    	public CopyAction(String name) {
    		super(name);
    		setEnabled(false);
    		setMnemonic('C');
    		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
    	}
    	public void actionPerformed(ActionEvent e) {
    		try{
    			getEditArea().copy();
    		}catch(Exception ex) {
    			System.out.println("Copy Exception!");
    		}
    		update();
    		getAction("剪切").update();
			getAction("删除").update();
    	}
    	protected void update() {
    		String text = null;
    		try{
    			text = getEditArea().getSelectedText();
    		}catch(Exception ex) {
    			System.out.println("Select Exception!");
    		}
    		if(text!=null && !Running) {
    			setEnabled(true);
    		}else{
    			setEnabled(false);
    		}
    	}
    }
    //粘贴Action
    class PasteAction extends SntAction {
    	public PasteAction() {
    		this("粘贴");
    	}
    	public PasteAction(String name) {
    		super(name);
    		setEnabled(false);
    		setMnemonic('P');
    		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
    	}
    	public void actionPerformed(ActionEvent e) {
    		try{
    			getEditArea().paste();
    		}catch(Exception ex) {
    			System.out.println("Paste Exception!");
    		}
    		update();
    		getAction("剪切").update();
			getAction("复制").update();
			getAction("删除").update();
    	}
    	protected void update() {
    		java.awt.datatransfer.DataFlavor flavor = java.awt.datatransfer.DataFlavor.stringFlavor;
    		if(clipboard.isDataFlavorAvailable(flavor) && !Running) {
    			setEnabled(true);
    		}else{
    			setEnabled(false);
    		}
    	}
    }
    //删除Action
    class DeleteAction extends SntAction {
    	public DeleteAction() {
    		this("删除");
    	}
    	public DeleteAction(String name) {
    		super(name);
    		setEnabled(false);
    		setMnemonic('L');
    		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
    	}
    	public void actionPerformed(ActionEvent e) {
    		try{
    			getEditArea().replaceSelection("");
    		}catch(Exception ex) {
    			System.out.println("Delete Exception!");
    		}
    		update();
    		getAction("剪切").update();
			getAction("复制").update();
    	}
    	protected void update() {
    		String text = null;
    		try{
    			text = getEditArea().getSelectedText();
    		}catch(Exception ex) {
    			System.out.println("Select Exception!");
    		}
    		if(text!=null && !Running) {
    			setEnabled(true);
    		}else{
    			setEnabled(false);
    		}
    	}
    }
    //查找Action
    class FindAction extends SntAction{
    	public FindAction() {
    		this("查找");
    	}
    	public FindAction(String name) {
    		super(name);
    		setEnabled(false);
    		setMnemonic('F');
    		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
    	}
    	public void actionPerformed(ActionEvent e) {
    		
    	}
    	protected void update() {
    		if(getEditArea().getText().length()>0 && !Running){
    			setEnabled(true);
    		}else{
    			setEnabled(false);
    		}
    	}
    }
    //查找下一个Action
    class FindNextAction extends SntAction{
    	public FindNextAction() {
    		this("查找下一个");
    	}
    	public FindNextAction(String name) {
    		super(name);
    		setEnabled(true);
    		setMnemonic('N');
    		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
    	}
    	public void actionPerformed(ActionEvent e) {
    		
    	}
    	protected void update() {
    		if(Running) {
				setEnabled(false);
			}else{
				setEnabled(true);
			}
    	}
    }
    //替换Action
    class ReplaceAction extends SntAction{
    	public ReplaceAction() {
    		this("替换");
    	}
    	public ReplaceAction(String name) {
    		super(name);
    		setEnabled(false);
    		setMnemonic('R');
    		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_MASK));
    	}
    	public void actionPerformed(ActionEvent e) {
    		
    	}
    	protected void update() {
    		if(getEditArea().getText().length()>0 && !Running){
    			setEnabled(true);
    		}else{
    			setEnabled(false);
    		}
    	}
    }
    //转到Action
    class GotoAction extends SntAction{
    	public GotoAction() {
    		this("转到");
    	}
    	public GotoAction(String name) {
    		super(name);
    		setEnabled(false);
    		setMnemonic('G');
    		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
    	}
    	public void actionPerformed(ActionEvent e) {
    		int pos = -1;
    		try{
    			int offset = Integer.parseInt(JOptionPane.showInputDialog(mainApp.this,"转到行:",1));
    			if(offset==1) {
    				pos = 0;
    			}else{
    				pos = getEditArea().getLineEndOffset(offset-2);
    			}
    		}catch(Exception ex) {
    			System.out.println("Goto Exception!");
    		}
    		if(pos>=0) {
    			getEditArea().setCaretPosition(pos);
    		}
    	}
    	protected void update() {
    		if(getEditArea().getText().length()>0 && !Running){
    			setEnabled(true);
    		}else{
    			setEnabled(false);
    		}
    	}
    }
    //全选
    class SelectAllAction extends SntAction{
    	public SelectAllAction() {
    		this("全选");
    	}
    	public SelectAllAction(String name) {
    		super(name);
    		setEnabled(false);
    		setMnemonic('A');
    		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
    	}
    	public void actionPerformed(ActionEvent e) {
    		getEditArea().selectAll();
    	}
    	protected void update() {
    		if(getEditArea().getText().length()>0 && !Running){
    			setEnabled(true);
    		}else{
    			setEnabled(false);
    		}
    	}
    }
    //时间日期Action
    class DateAction extends SntAction{
    	public DateAction() {
    		this("时间/日期");
    	}
    	public DateAction(String name) {
    		super(name);
    		setEnabled(true);
    		setMnemonic('D');
    		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
    	}
    	public void actionPerformed(ActionEvent e) {
    		Date Now = Calendar.getInstance().getTime();
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
    		getEditArea().append(format.format(Now));
    	}
    	protected void update() {
    		if(Running) {
				setEnabled(false);
			}else{
				setEnabled(true);
			}
    	}
    }
    
    //皮肤切换Action
    class UIChangeAction extends AbstractAction {
    	String skin;
    	public UIChangeAction(String name,String sk) {
    		super(name);
    		this.skin = sk;
    	}
    	public void actionPerformed(ActionEvent e) {
    		String usingSkin = UIManager.getLookAndFeel().toString();
    		if(usingSkin.indexOf(skin)==-1) {
    			try{
	    			UIManager.setLookAndFeel(skin);
	    			SwingUtilities.updateComponentTreeUI(mainApp.this);
	    		}catch(Exception ex) {
	    			System.out.println("更改皮肤失败"+skin);
	    		}
    		}
    	}
    }
    
//*****************线程区********************//
	//打开线程
	class OpenThread implements Runnable {
		private LineNumberReader buf;
		private int size = 0;
		private int TotalSize = 0;
		public OpenThread(File f) {
			try{
				buf = new LineNumberReader(new BufferedReader(
			    	new InputStreamReader(new FileInputStream(f))));
			    int i=0;
				TotalSize = (int)f.length();
				getEditArea().setText("");
				status.progressBar.setMaximum(TotalSize-1);
				status.progressBar.setMinimum(0);
				status.setProgressBarVisible(true);
				Running = true;
    			fireMenuEnabledChange();
				new Thread(this).start();
			}catch(Exception ex) {
				System.out.println("打开线程异常!");
			}
		}
		private int getStringSize(String str) {
			int s = 0;
			for(int i=0; i<str.length(); i++) {
				s = (str.charAt(i)<=256)?s+1:s+2;
			}
			return s;
		}
		private void setValue(final int n) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
     			public void run() {
					status.setProgressBarValue(n);
				}
    		});
		}
		public synchronized void run() {
			if(buf!=null) {
				try{
		    		String tmp;
		    		while( (tmp=buf.readLine())!=null ) {
			    		getEditArea().append(formatString(tmp));
			    		int n = getStringSize(tmp);
			    		if(n>0) {
			    			size+=n;
			    			setValue(size);
			    		}
			    	}
			    	setValue(TotalSize-1);
			    	status.setProgressBarVisible(false);
		    	}catch(Exception e){
		    		System.out.println("打开线程异常!");
		    	}
			}
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
	    			Running = false;
    				fireMenuEnabledChange();
	    			getEditArea().getDocument().addUndoableEditListener(undoHandler);
				}
			});
	    	Saved = true;
		}
	}
	
	//保存线程
	class SaveThread implements Runnable {
		File opfile;
		int line;
		public SaveThread(File file) {
			this.opfile = file;
			Running = true;
    		fireMenuEnabledChange();
    		line = getEditArea().getLineCount();
			status.progressBar.setMaximum(line-1);
			status.progressBar.setMinimum(0);
			new Thread(this).start();
		}
		private void setValue(final int n) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
     			public void run() {
					status.setProgressBarValue(n);
				}
    		});
		}
		public void run() {
    		PrintWriter pw = null;
    		try{
				pw = new PrintWriter(new FileWriter(opfile));
				int i = 0;
				status.progressBar.setVisible(true);
				while(i<line) {
					pw.print(formatString(getStringOfLine(i)));
					setValue(i);
					i++;
				}
				setValue(line-1);
				status.progressBar.setVisible(false);
    		}catch(Exception ex){}
    		javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
		    		Running = false;
		    		fireMenuEnabledChange();
		    		}
			});
    		Saved = true;
    		status.updateTitle(opfile.getName());
    		if(pw!=null)
    			pw.close();
		}
	}
    
//*****************程序入口********************//
    public static void main(String args[]) {
    	FrameProperty.setDefaultSkin(FrameProperty.SUBSTANCE);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
     		public void run() {
	        	mainApp MF = new mainApp();
	       		MF.setVisible(true);
      		}
    	});
    }
}
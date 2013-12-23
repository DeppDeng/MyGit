package pers.diskMng;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Notepad /*
					 * implements ActionListener , MouseListener ,
					 * MouseMotionListener , WindowListener , ItemListener ,
					 * KeyListener, TextListener
					 */
{
	// 成员变量
	private Frame mainFrame;// 主框架
	private MenuBar mb; // 菜单条
	private Menu mFile, mEdit, mFormat, mHelp; // 菜单：文件，编辑，格式，帮助
	private MenuItem miNew, miOpen, miSave, miSaveAs, miExit;// 文件菜单项：新建，打开，保存，另存为，退出
	private MenuItem miCut, miCopy, miPaste, miDelete;// 编辑菜单项：剪切，复制，粘贴，删除
	private MenuItem miFont, miLowtoCapital, miCapitaltoLow, miEncrypt,
			miDisencrypt;// 格式菜单项：字体
	private MenuItem miAboutNotepad;// 帮助菜单项：关于记事本

	private TextArea ta;// 文本区

	private String tempString;// 临时字符串,用于存储需要复制粘贴的字符串

	private boolean textValueChanged = false;

	private int id_font;// 字体

	String fileName = "";// 上次保存后的文件名和地址

	// 构造函数
	public Notepad() {

		// 框架
		mainFrame = new Frame("Notepad v0.99       by Launching");
		mb = new MenuBar();
		ta = new TextArea(30, 60);
		ta.setFont(new Font("Times New Rome", Font.PLAIN, 15));
		ta.setBackground(new Color(0, 250, 200));

		// 菜单条
		mFile = new Menu("File");
		mEdit = new Menu("Edit");
		mFormat = new Menu("Format");
		mHelp = new Menu("Help");

		// "文件"
		miNew = new MenuItem("New");
		miOpen = new MenuItem("Open");
		miSave = new MenuItem("Save");
		miSaveAs = new MenuItem("Save as");
		miExit = new MenuItem("Exit");

		// "编辑"
		miCut = new MenuItem("Cut");
		miCopy = new MenuItem("Copy");
		miPaste = new MenuItem("Paste");
		miDelete = new MenuItem("Delete");

		// "格式"
		miFont = new MenuItem("Font");
		miLowtoCapital = new MenuItem("Low to Capital");
		miCapitaltoLow = new MenuItem("Capital to Low");
		miEncrypt = new MenuItem("Encrypt");
		miDisencrypt = new MenuItem("Disencrypt");

		// "帮助"
		miAboutNotepad = new MenuItem("About Notepad");

		// 添加文件菜单项
		mFile.add(miNew);
		mFile.add(miOpen);
		mFile.add(miSave);
		mFile.add(miSaveAs);
		mFile.add(miExit);

		// 添加编辑菜单项
		mEdit.add(miCut);
		mEdit.add(miCopy);
		mEdit.add(miPaste);
		mEdit.add(miDelete);

		// 添加格式菜单项
		mFormat.add(miFont);
		mFormat.add(miLowtoCapital);
		mFormat.add(miCapitaltoLow);
		mFormat.add(miEncrypt);
		mFormat.add(miDisencrypt);

		// 添加帮助菜单项
		mHelp.add(miAboutNotepad);

		// 菜单条添加菜单
		mb.add(mFile);
		mb.add(mEdit);
		mb.add(mFormat);
		mb.add(mHelp);

		// 框架添加菜单条
		mainFrame.setMenuBar(mb);

		// 初始字符串赋为空
		tempString = "";

		// 添加文本区
		mainFrame.add(ta, BorderLayout.CENTER);

		mainFrame.setSize(800, 500);
		mainFrame.setLocation(100, 100);// 起始位置
		mainFrame.setResizable(true);// 不可更改大小
		mainFrame.setVisible(true);
		// mainFrame.pack();

		// ////////////////////////////////////////////////////////////////////////////////
		// //////////////////////////////增加监视器////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////////////////

		// 主框架
		mainFrame.addWindowListener(new WindowAdapter() { // 关闭窗口
					public void windowClosing(WindowEvent e) {
						System.exit(0);
					}
				});

		// 文本区
		ta.addKeyListener(new KeyAdapter() {
			public void KeyTyped(KeyEvent e) {
				textValueChanged = true; // 键盘按键按下即导致文本修改
			}
		});

		// //////////////"文件"菜单：//////////////////////

		// 新建
		miNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				ta.replaceRange("", 0, ta.getText().length());// 清空文本区的内容

				fileName = "";// 文件名清空
			}
		});

		// 打开
		miOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				FileDialog d = new FileDialog(mainFrame, "open file",
						FileDialog.LOAD);// 打开文件对话框

				d.addWindowListener(new WindowAdapter() { // 关闭文件对话框窗口
					public void windowClosing(WindowEvent ee) {
						System.exit(0);
					}
				});
				d.setVisible(true);

				File f = new File(d.getDirectory() + d.getFile()); // 建立新文件

				fileName = d.getDirectory() + d.getFile();// 得到文件名

				char ch[] = new char[(int) f.length()];// /用此文件的长度建立一个字符数组

				try// 异常处理
				{
					// 读出数据，并存入字符数组ch中
					BufferedReader bw = new BufferedReader(new FileReader(f));
					bw.read(ch);
					bw.close();
				} catch (FileNotFoundException fe) {
					System.out.println("file not found");
					System.exit(0);
				} catch (IOException ie) {
					System.out.println("IO error");
					System.exit(0);
				}

				String s = new String(ch);

				ta.setText(s);// 设置文本区为所打开文件的内容
			}
		});

		// 保存
		miSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (fileName.equals("")) { // 如果文件没有被保存过,即文件名为空

					FileDialog d = new FileDialog(mainFrame, "save file",
							FileDialog.SAVE);// 保存文件对话框

					d.addWindowListener(new WindowAdapter() { // 关闭文件对话框窗口
						public void windowClosing(WindowEvent ee) {
							System.exit(0);
						}
					});
					d.setVisible(true);

					String s = ta.getText();// 得到所输入的文本内容

					try// 异常处理
					{
						File f = new File(d.getDirectory() + d.getFile());// 新建文件

						fileName = d.getDirectory() + d.getFile();// 得到文件名

						BufferedWriter bw = new BufferedWriter(
								new FileWriter(f));// 输入到文件中
						bw.write(s, 0, s.length());
						bw.close();

					} catch (FileNotFoundException fe_) {
						System.out.println("file not found");
						System.exit(0);
					} catch (IOException ie_) {
						System.out.println(" IO error");
						System.exit(0);
					}

				}

				else // 如果文件已经保存过
				{
					String s = ta.getText();// 得到所输入的文本内容

					try// 异常处理
					{
						File f = new File(fileName);// 新建文件

						BufferedWriter bw = new BufferedWriter(
								new FileWriter(f));// 输入到文件中
						bw.write(s, 0, s.length());
						bw.close();

					} catch (FileNotFoundException fe_) {
						System.out.println("file not found");
						System.exit(0);
					} catch (IOException ie_) {
						System.out.println(" IO error");
						System.exit(0);
					}

				}

			}
		});

		// 另存为
		miSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileDialog d = new FileDialog(mainFrame, "save file",
						FileDialog.SAVE);// 保存文件对话框

				d.addWindowListener(new WindowAdapter() { // 关闭文件对话框窗口
					public void windowClosing(WindowEvent ee) {
						System.exit(0);
					}
				});
				d.setVisible(true);

				String s = ta.getText();// 得到所输入的文本内容

				try// 异常处理
				{
					File f = new File(d.getDirectory() + d.getFile());// 新建文件

					BufferedWriter bw = new BufferedWriter(new FileWriter(f));// 输入到文件中
					bw.write(s, 0, s.length());
					bw.close();

				} catch (FileNotFoundException fe_) {
					System.out.println("file not found");
					System.exit(0);
				} catch (IOException ie_) {
					System.out.println(" IO error");
					System.exit(0);
				}
			}
		});

		// 退出
		miExit.addActionListener(new ActionListener() { // /退出程序
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		// //////////////"编辑"菜单：////////////////////

		// 剪切
		miCut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tempString = ta.getSelectedText(); // /得到要复制的内容,暂存在tempString中
				StringBuffer tmp = new StringBuffer(ta.getText());// 临时存储文本
				int start = ta.getSelectionStart(); // 得到要删除的字符串的起始位置
				int len = ta.getSelectedText().length(); // 得到要删除的字符串的长度
				tmp.delete(start, start + len); // /删除所选中的字符串
				ta.setText(tmp.toString());// 用新文本设置原文本
			}
		});

		// 复制
		miCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tempString = ta.getSelectedText(); // /得到要复制的内容,暂存在tempString中
			}
		});

		// 粘贴
		miPaste.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringBuffer tmp = new StringBuffer(ta.getText());// 临时存储文本
				int start = ta.getSelectionStart(); // 得到要粘贴的位置
				tmp.insert(start, tempString);// 查入要粘贴的内容
				ta.setText(tmp.toString());// 用新文本设置原文本
			}
		});

		// 删除
		miDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringBuffer tmp = new StringBuffer(ta.getText());// 临时存储文本
				int start = ta.getSelectionStart(); // 得到要删除的字符串的起始位置
				int len = ta.getSelectedText().length(); // 得到要删除的字符串的长度
				tmp.delete(start, start + len); // /删除所选中的字符串
				ta.setText(tmp.toString());// 用新文本设置原文本
			}
		});

		// //////////////"格式"菜单：////////////////////

		// 字体
		miFont.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final Dialog d = new Dialog(mainFrame, "Font");// 新建对话框
				d.setLocation(250, 250);// 起始位置

				d.setLayout(new BorderLayout());// 表格布局

				// ////////////////////////上部分面板
				Label l_font = new Label("font");// font标签
				Panel p_1 = new Panel();
				p_1.add(l_font);
				p_1.setVisible(true);

				// ////////////////////////中部分面板
				List font_list = new List(6, false);// 字体列表

				// 添加字体项目
				font_list.add("Plain");// /普通字体
				font_list.add("Bold"); // /粗体
				font_list.add("Italic");// 斜体

				font_list.addItemListener(new MyItemListener_font()); // 字体增加监视器

				Panel p_2 = new Panel();
				p_2.add(font_list);
				p_2.setVisible(true);

				// ////////////////////////下部分面板
				Button ok = new Button("OK");
				ok.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						d.dispose();
					}
				});
				ok.setSize(new Dimension(20, 5));

				Panel p_3 = new Panel();// 下部分面板
				p_3.add(ok);
				p_3.setVisible(true);

				// 添加三个面板
				d.add(p_1, BorderLayout.NORTH);
				d.add(p_2, BorderLayout.CENTER);
				d.add(p_3, BorderLayout.SOUTH);
				d.pack();

				d.addWindowListener(new WindowAdapter() { // 关闭对话框窗口
					public void windowClosing(WindowEvent ee) {
						d.dispose();
					}
				});

				d.setVisible(true);
			}
		});

		// 小写字母转大写
		miLowtoCapital.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = ta.getText();// 得到所输入的文本内容
				StringBuffer temp = new StringBuffer("");
				for (int i = 0; i < s.length(); i++) {
					if ((int) s.charAt(i) >= 97 && (int) s.charAt(i) <= 122) {
						temp.append((char) ((int) s.charAt(i) - 32));
					} else
						temp.append(s.charAt(i));
				}
				s = new String(temp);
				ta.setText(s);
			}
		});

		// 大写字母转小写
		miCapitaltoLow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = ta.getText();// 得到所输入的文本内容
				StringBuffer temp = new StringBuffer("");
				for (int i = 0; i < s.length(); i++) {
					if ((int) s.charAt(i) >= 65 && (int) s.charAt(i) <= 90) {
						temp.append((char) ((int) s.charAt(i) + 32));
					} else
						temp.append(s.charAt(i));
				}
				s = new String(temp);
				ta.setText(s);
			}
		});

		// 加密
		miEncrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = ta.getText();// 得到所输入的文本内容
				StringBuffer temp = new StringBuffer("");
				for (int i = 0; i < s.length(); i++) {
					if (s.charAt(i) >= 40 && s.charAt(i) <= 125) {
						if (i % 2 == 0) {
							temp.append((char) (s.charAt(i) + 1));
						} else
							temp.append((char) (s.charAt(i) - 1));
					} else
						temp.append(s.charAt(i));

				}
				s = new String(temp);
				ta.setText(s);
			}
		});

		// 解密
		miDisencrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = ta.getText();// 得到所输入的文本内容
				StringBuffer temp = new StringBuffer("");
				for (int i = 0; i < s.length(); i++) {
					if (s.charAt(i) >= 40 && s.charAt(i) <= 125) {
						if (i % 2 == 0) {
							temp.append((char) (s.charAt(i) - 1));
						} else
							temp.append((char) (s.charAt(i) + 1));
					} else
						temp.append(s.charAt(i));
				}
				s = new String(temp);
				ta.setText(s);
			}
		});

		// //////////////"帮助"菜单：////////////////////

		// 关于记事本
		miAboutNotepad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final Dialog d = new Dialog(mainFrame, "AboutNotepad");// 新建对话框
				TextArea t = new TextArea("\nwelcome to use Notepad " + "\n\n"
						+ "Copyright@Launching " + "\n\n" + "free software"
						+ "\n\n" + "v0.99");// 添加标签
				t.setSize(new Dimension(5, 5));
				t.setEditable(false);
				d.setResizable(false);// 不可调整大小
				d.add(t);
				d.pack();

				d.addWindowListener(new WindowAdapter() { // 关闭对话框窗口
					public void windowClosing(WindowEvent ee) {
						d.dispose();
					}
				});
				d.setLocation(100, 250);// 起始位置
				d.setVisible(true);
			}
		});

	}

	class MyItemListener_font implements ItemListener { // 字体监听器
		public void itemStateChanged(ItemEvent e) {
			id_font = ((java.awt.List) e.getSource()).getSelectedIndex();
			switch (id_font) {
			case 0: {
				ta.setFont(new Font("Times New Roman", Font.PLAIN, ta.getFont()
						.getSize()));// 普通文字
				break;
			}
			case 1: {
				ta.setFont(new Font("Times New Roman", Font.BOLD, ta.getFont()
						.getSize()));// 粗体文字
				break;
			}
			case 2: {
				ta.setFont(new Font("Times New Roman", Font.ITALIC, ta
						.getFont().getSize()));// 斜体文字
				break;
			}
			}
		}
	}

	// ///////////////////////////////////////主函数///////////////////////////////////////////////////
	public static void main(String arg[]) {
		Notepad test = new Notepad(); // /创建记事本
	}
	// ////////////////////////////////////////////////////////////////////////////////////////////////
}
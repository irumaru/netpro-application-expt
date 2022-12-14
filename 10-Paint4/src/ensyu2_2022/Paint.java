package ensyu2_2022;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;


//フレームオブジェクトを継承した、Paintクラスを宣言
public class Paint extends Frame implements MouseListener,MouseMotionListener{
	//FigureインスタンスとFigureクラスを継承したクラスのインスタンスを格納するリスト状のプロパティを、ArrayList型で宣言
	//全ての図形は、objListへ格納される
	ArrayList<Figure> objList;
	//図形の基準点x,yプロパティを宣言
	int x, y;
	
	//CheckboxGroup cbg; //メニュー
	//Checkbox c1, c2, c3, c4; //メニューの要素
	//Button end; //終了ボタン
	int mode = 0; //描画モード(1: 1点指定図形, 2: 2点指定図形, 3: n点指定)
	//クリック時間
	long latestClick = 0;
	
	//上記と同じインスタンスを格納するプロパティを宣言
	//実際に描画する図形
	Figure obj;
	Figure objSelect = null;
	
	//描画時刻
	long now, old;
	
	//※1で利用するプロパティを宣言
	// TextFieldUtility size;
	// TextFieldUtility width;
	// TextFieldUtility height;
	ChoiceFieldUtility fill;
	ChoiceFieldUtility shape;
	ChoiceFieldUtility operation;
	ColorPickerUtility color;
	UndoButton undo;
	RedoButton redo;
	ClearButton clear;
	Save save;
	Load load;
	ExportImage export;
	
	private Image offImage;
	private Graphics gv;
	
	//mainクラスメソッドを宣言(起動時に実行される)
	public static void main(String[] args){
		//ペイントインスタンスを作成して格納
		Paint f = new Paint();
		//ウィンドウサイズを設定
		f.setSize(800,600);
		//画面上の設定メニューを中央上から配置するように指定
		f.setLayout(new FlowLayout(FlowLayout.CENTER));
		//ウィンドウのタイトルを設定
		f.setTitle("ペイントアプリ");
		//終了時の処理を設定
		f.addWindowListener(new WindowAdapter(){
			@Override public void windowClosing(WindowEvent e){
				System.exit(0);
			}});
		//ウィンドウの表示
		f.setVisible(true);
	}
	
	//コンストラクタの宣言(Paintインスタンス作成時に実行される)
	Paint(){
		
		//Figure型のインスタンスをリスト上に格納できるArrayListインスタンスを作成し、objListプロパティへ代入
		objList = new ArrayList<Figure>();
		
		addMouseListener(this);
		addMouseMotionListener(this);

		//setLayout(null);
		//画面作成
		/*
		cbg = new CheckboxGroup(); //Checkboxの集合を作成
		c1 = new Checkbox("丸", cbg, true); //丸メニューの作成
		c1.setBounds(560, 30, 60, 30); //丸メニューの座標指定
		add(c1); //丸メニューの追加
		
		c2 = new Checkbox("円", cbg, false);
		c2.setBounds(560, 60, 60, 30);
		add(c2);
		
		c3 = new Checkbox("四角", cbg, false);
		c3.setBounds(560, 90, 60, 30);
		add(c3); 
		
		c4 = new Checkbox("線", cbg, false);
		c4.setBounds(560, 120, 60, 30);
		add(c4);
		
		end = new Button("終了");
		end.setBounds(560, 300, 60, 30);
		add(end);
		*/
		
		//※1で用意したプロパティへ、各種インスタンスを代入
		
		//画面上に最大図形数の設定項目を追加
		//初期値は0であり、0は無制限を意味する。
		// size = new TextFieldUtility(this, "オブジェクト数", "0");
		//画面上に図形の幅の設定項目を追加
		// width = new TextFieldUtility(this, "幅", "80");
		//画面上に図形の高さの設定項目を追加
		// height = new TextFieldUtility(this, "高さ", "80");
		//画面上に塗りつぶしの設定項目を追加
		//操作方法を選択
		operation = new ChoiceFieldUtility(this, "操作", new String []{"描画", "移動"});
		//引数でメニュー内容を指示
		fill = new ChoiceFieldUtility(this, "塗りつぶし", new String []{"なし", "塗りつぶし"});
		//画面上に図形の設定項目を追加
		shape = new ChoiceFieldUtility(this, "図形", new String []{"丸", "円", "楕円", "四角", "多角形", "線", "折れ線"});
		//画面上にカラーピッカーを開いて色を選択するボタンを追加
		color = new ColorPickerUtility(this);
		//Undo
		undo = new UndoButton(this, objList);
		//Redo
		redo = new RedoButton(this, objList, undo);
		//Clear
		clear = new ClearButton(this, objList);
		//Load
		load = new Load(this, objList);
		//Save
		save = new Save(this, objList, load);
		//ExportImage
		export = new ExportImage(this);
		
		//終了ボタン処理の登録
		//end.addActionListener(this);
		
		//描画時刻の初期化
		now = old = 0;
		
		width = height = 0;
	}
	
	Integer width, height, oldWidth, oldHeight;
	
	//描画(フレームごと)
	@Override public void paint(Graphics g){
		//ダブルバッファリング
		//http://www.gamesite8.com/archives/615401.html
		
		oldWidth = width;
		oldHeight = height;
		width = getSize().width;
		height = getSize().height;
		
		//イメージバッファ生成
		if(offImage == null || !height.equals(oldHeight) || !width.equals(oldWidth)) {
			System.out.println("update");
			offImage = createImage(width, height);
			gv = offImage.getGraphics();
		}
		
		//ウィンドウに合わせて四角で初期化
		gv.clearRect(0, 0, width, height);
		
		//各種図形を描画
		Figure f;
		for(int i = 0; i < objList.size(); i ++) {
			f = objList.get(i);
			f.paint(gv);
		}
		
		if(mode >= 1) obj.paint(gv);
		
		//バッファされたイメージを描画
		g.drawImage(offImage, 0, 0, width, height, this);
	}
	
	public void repaint() {
		//描画を安定させるため、FPSを制限
		now = System.currentTimeMillis();
		if(33 < this.now - this.old) {
			old = now;
			super.repaint();
		}
	}
	
	//押されたとき
	@Override public void mousePressed(MouseEvent e){
		x = e.getX();
		y = e.getY();
		
		int o = operation.getChoice();
		
		if(o == 0) {
			if(mode == 3) {
				//ダブルクリック(2回目)で描画終了
				if(System.currentTimeMillis() - latestClick < 300) {
					//描画なし
					mode = 0;
					objList.add(obj);
					obj = null;
					//再描画
					repaint();
					//終了
					return;
				}
				latestClick = System.currentTimeMillis();
				
				//点の追加
				obj.addCoord(x, y);
				
				repaint();
				
				return;
			}
			
			//描画開始
			Integer s = shape.getChoice();
			
			if(s == 0) {//描画
				mode = 1;
				obj = new Dot();
			}else if(s == 1) { //円
				mode = 2;
				obj = new Circle();
			}else if(s == 2) {
				mode = 2;
				obj = new Oval();
			}else if(s == 3) { //四角
				mode = 2;
				obj = new Rect();
			}else if(s == 4) {//多角形
				mode = 3;
				obj = new Polygon();
			}else if(s == 5) { //線
				mode = 2;
				obj = new Line();
			}else if(s == 6) { //折れ線
				mode = 3;
				obj = new Polyline();
			}else {
				System.err.println("存在しない図形番号です。");
				System.exit(1);
			}
			
			if(obj != null) {
				obj.moveto(x, y);
				obj.setColor(color.getColor());
				obj.setFill(fill.getChoice() == 1);
			}
		}else if(o == 1) {//移動
			//一致する最初の図形を取得
			Figure objt;
			for(int i = 0; i < objList.size(); i ++) {
				objt = objList.get(i);
				if(objt.x < x && x < objt.x + objt.width && objt.y < y && y < objt.y + objt.height) {
					objSelect = objt;
					break;
				}
			}
		}
		
		repaint();
		
		//図形数を表示
		//System.out.println("オブジェクト数: " + objList.size());
		
		//ディバッグ
		//for(int i = 0; i < objList.size(); i ++) {
		//	System.out.println("i="+i+" width="+objList.get(i).getSize()[0]);
		//}
		System.out.println();
	}
	//離されたとき
	@Override public void mouseReleased(MouseEvent e){
		x = e.getX();
		y = e.getY();
		
		int o = operation.getChoice();
		
		if(o == 0) {
			if(mode == 1) {
				obj.moveto(x, y);
			}else if(mode == 2) {
				obj.setWH(x - obj.x, y - obj.y);
			}
			
			if(mode == 1 || mode == 2) {
				objList.add(obj);
				obj = null;
				mode = 0;
			}
		}else if(o == 1) {
			if(objSelect != null) {
				objSelect = null;
			}
		}
		
		repaint();
	}
	//クリックされた
	@Override public void mouseClicked(MouseEvent e){}
	//Windowに入った
	@Override public void mouseEntered(MouseEvent e){}
	//WIndowを出た
	@Override public void mouseExited(MouseEvent e){}
	//ボタンを押したまま移動
	@Override public void mouseDragged(MouseEvent e) {
		x = e.getX();
		y = e.getY();
		
		int o = operation.getChoice();
		
		if(o == 0) {
			if(mode == 1) {
				obj.moveto(x, y);
			}else if(mode == 2) {
				obj.setWH(x - obj.x, y - obj.y);//幅と高さの指定
			}
		}else if(o == 1) {
			if(objSelect != null) {
				objSelect.moveto(x, y);
			}
		}

		repaint();
	}
	//移動
	@Override public void mouseMoved(MouseEvent e){
		x = e.getX();
		y = e.getY();
		
		//仮の座標を設定
		if(mode == 3) {
			obj.addVirtualCoord(x, y);
		}

		repaint();
	}
}

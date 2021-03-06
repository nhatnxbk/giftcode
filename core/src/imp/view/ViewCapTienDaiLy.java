package imp.view;

import utils.factory.AppPreference;
import utils.factory.Factory;
import utils.factory.FontFactory.fontType;
import utils.factory.StringUtil;
import utils.factory.Style;
import utils.keyboard.KeyboardConfig;
import utils.networks.ExtParamsKey;
import utils.networks.Request;
import utils.networks.UserInfo;
import utils.screen.AbstractGameScreen;
import utils.screen.Toast;

import com.aia.appsreport.component.table.AbstractTable;
import com.aia.appsreport.component.table.ItemInfoDaily;
import com.aia.appsreport.component.table.ItemTable;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.coder5560.game.assets.Assets;
import com.coder5560.game.enums.Constants;
import com.coder5560.game.enums.ViewState;
import com.coder5560.game.listener.OnCompleteListener;
import com.coder5560.game.ui.CustomTextField;
import com.coder5560.game.ui.DialogCustom;
import com.coder5560.game.ui.ItemListener;
import com.coder5560.game.ui.Loading;
import com.coder5560.game.ui.Page;
import com.coder5560.game.ui.TextfieldStatic;
import com.coder5560.game.views.TraceView;
import com.coder5560.game.views.View;

public class ViewCapTienDaiLy extends View {

	private ViewDetail		viewDetail;
	private CustomTextField	tfPerson;

	private AbstractTable	tableDaily;
	private Page			page;

	private JsonValue		responeGetDaily;
	private JsonValue		responeGetDailyLower;
	private JsonValue		responeCheck;

	@Override
	public String getLabel() {
		return "Cấp tiền cho đại lý";
	}

	public void buildComponent() {
		this.top();
		setBackground(new NinePatchDrawable(new NinePatch(
				Assets.instance.ui.reg_ninepatch)));

		Label lbPerson = new Label("Người nhận tiền", new LabelStyle(
				Assets.instance.fontFactory.getFont(20, fontType.Regular),
				Color.BLACK));
		tfPerson = new CustomTextField("", Style.ins.getTextFieldStyle(8,
				Assets.instance.fontFactory.getFont(25, fontType.Light)));
		tfPerson.setOnscreenKeyboard(AbstractGameScreen.keyboard);
		tfPerson.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				AbstractGameScreen.keyboard.registerTextField(tfPerson,
						KeyboardConfig.NORMAL, KeyboardConfig.SINGLE_LINE);
				return super.touchDown(event, x, y, pointer, button);
			}
		});
		final CustomTextField tfMoney = new CustomTextField(
				"",
				Style.ins.getTextFieldStyle(8,
						Assets.instance.fontFactory.getFont(25, fontType.Light)));
		tfMoney.setOnscreenKeyboard(AbstractGameScreen.keyboard);
		tfMoney.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				AbstractGameScreen.keyboard.registerTextField(tfMoney,
						KeyboardConfig.NORMAL, KeyboardConfig.SINGLE_LINE);
				return super.touchDown(event, x, y, pointer, button);
			}
		});

		TextButton btSend = new TextButton("Tìm kiếm",
				Style.ins.textButtonStyle);
		btSend.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				if (tfPerson.getText().equalsIgnoreCase("")) {
					AbstractGameScreen.keyboard.hide();
					Toast.makeText(_stage, "Vui lòng nhập đủ thông tin",
							Toast.LENGTH_SHORT);
				} else {
					AbstractGameScreen.keyboard.hide();
					Loading.ins.show(ViewCapTienDaiLy.this);
					Request.getInstance().checkDaily(
							AppPreference.instance.name, tfPerson.getText(),
							new CheckDaily());
				}
			}
		});

		Label lbTitle = new Label("Danh sách các đại lý cấp dưới",
				new LabelStyle(Assets.instance.fontFactory.getFont(30,
						fontType.Medium), Color.BLUE));

		float[] widthCol = { 50, 180, 150, 98 };
		tableDaily = new AbstractTable(new Table(), widthCol);
		String[] title = { "STT", "Tên đại lý", "Số điện thoại", "" };
		tableDaily.setTitle(title);
		tableDaily.setCancelTouchFocus(false);

		page = new Page(getWidth(), 60);
		page.setListener(new ItemListener() {
			@Override
			public void onItemClick() {
				tableDaily.setScrollX(0);
				tableDaily.setScrollY(0);
				tableDaily.addAction(Actions.sequence(
						Actions.alpha(0, 0.5f, Interpolation.exp5Out),
						Actions.alpha(1, 0.5f, Interpolation.exp5Out)));
				tableDaily.removeAll();
				for (int i = 0; i < page.getCurrentDataPage().size(); i++) {
					ItemTable item = page.getCurrentDataPage().get(i);
					tableDaily.addItem(item);
				}
			}
		});

		viewDetail = new ViewDetail();

		this.padTop(15);
		this.add(lbPerson).right();
		this.add(tfPerson).padLeft(10).width(250).height(35).left().row();
		this.add(btSend).padTop(10).width(120).height(40).colspan(2).row();
		this.add(lbTitle).padTop(15).colspan(2).row();
		this.add(tableDaily).padTop(10).height(540).colspan(2).row();
		this.add(page).colspan(2);
		this.addActor(viewDetail);

		Loading.ins.show(this);
		Request.getInstance().getLowerDaily(AppPreference.instance.name,
				AppPreference.instance.pass, 1, new GetDailyLower());
	}

	@Override
	public void update(float delta) {
		viewDetail.update(delta);
		if (responeGetDailyLower != null) {
			Loading.ins.hide();
			page.removeAllPage();
			tableDaily.removeAll();
			boolean result = responeGetDailyLower
					.getBoolean(ExtParamsKey.RESULT);
			if (result) {
				JsonValue listDaily = responeGetDailyLower
						.get(ExtParamsKey.LIST);
				if (listDaily.size > 0) {
					for (int i = 0; i < listDaily.size; i++) {
						JsonValue content = listDaily.get(i);
						final String nameDaily = content
								.getString(ExtParamsKey.FULL_NAME);
						final String address = content
								.getString(ExtParamsKey.ADDRESS);
						final String level = content
								.getString(ExtParamsKey.ROLE_NAME);
						final String phone = content
								.getString(ExtParamsKey.AGENCY_NAME);
						final String sdtGt = content
								.getString(ExtParamsKey.REF_CODE);
						final int money = content.getInt(ExtParamsKey.AMOUNT);
						final String currency = content
								.getString(ExtParamsKey.CURRENCY);
						final String email = content
								.getString(ExtParamsKey.EMAIL);
						final String deviceId = Factory.getDeviceID(content);
						final String deviceName = Factory.getDeviceName(content);
						int state = content.getInt(ExtParamsKey.STATE);
						final String realState;
						if (state == 0) {
							realState = "Chưa kích hoạt";
						} else if (state == 1) {
							realState = "Hoạt động bình thường";
						} else {
							realState = "Bị khóa";
						}
						ItemInfoDaily item = new ItemInfoDaily(tableDaily,
								new String[] { "" + (i + 1), nameDaily, phone });
						item.btSee.addListener(new ClickListener() {
							@Override
							public void clicked(InputEvent event, float x,
									float y) {
								super.clicked(event, x, y);
								String[] info = { nameDaily, address, level,
										phone, sdtGt, money + " " + currency,
								email, deviceId, deviceName, realState };
								viewDetail.money = money;
								viewDetail.agencyReceive = phone;
								viewDetail.setInfo(info);
								viewDetail.show(null);
							}
						});
						page.addData(item);
					}
					page.init();
					for (int i = 0; i < page.getCurrentDataPage().size(); i++) {
						ItemTable item = page.getCurrentDataPage().get(i);
						tableDaily.addItem(item);
					}
				} else {
					Toast.makeText(_stage, "Không có đại lý cấp dưới nào",
							Toast.LENGTH_SHORT);
				}
			} else {
				String mess = responeGetDailyLower
						.getString(ExtParamsKey.MESSAGE);
				Toast.makeText(_stage, mess, Toast.LENGTH_SHORT);
			}
			responeGetDailyLower = null;
		}

		if (responeCheck != null) {
			boolean result = responeCheck.getBoolean(ExtParamsKey.RESULT);
			if (result) {
				Request.getInstance().getInfoDaily(tfPerson.getText(),
						new GetDaily());
			} else {
				Loading.ins.hide();
				String mess = responeCheck.getString(ExtParamsKey.MESSAGE);
				Toast.makeText(_stage, mess, Toast.LENGTH_SHORT);
			}
			responeCheck = null;
		}

		if (responeGetDaily != null) {
			Loading.ins.hide();
			boolean result = responeGetDaily.getBoolean(ExtParamsKey.RESULT);
			if (result) {
				String nameDaily = responeGetDaily
						.getString(ExtParamsKey.FULL_NAME);
				String address = responeGetDaily
						.getString(ExtParamsKey.ADDRESS);
				String capDaily = responeGetDaily
						.getString(ExtParamsKey.ROLE_NAME);
				String sdtGt = responeGetDaily.getString(ExtParamsKey.REF_CODE);
				int money = responeGetDaily.getInt(ExtParamsKey.AMOUNT);
				String currency = responeGetDaily
						.getString(ExtParamsKey.CURRENCY);
				String email = responeGetDaily.getString(ExtParamsKey.EMAIL);
				String deviceId = Factory.getDeviceID(responeGetDaily);
				String deviceName = Factory.getDeviceName(responeGetDaily);
				int state = responeGetDaily.getInt(ExtParamsKey.STATE);
				String stringState;
				if (state == 0) {
					stringState = "Chưa kích hoạt";
				} else if (state == 1) {
					stringState = "Hoạt động bình thường";
				} else {
					stringState = "Bị khóa";
				}

				String[] info = { nameDaily, address, capDaily,
						tfPerson.getText(), sdtGt, money + " " + currency,
						email, deviceId, deviceName, stringState };
				viewDetail.money = money;
				viewDetail.agencyReceive = tfPerson.getText();
				viewDetail.setInfo(info);
				viewDetail.show(null);
			} else {
				String mess = responeGetDaily.getString(ExtParamsKey.MESSAGE);
				Toast.makeText(_stage, mess, Toast.LENGTH_SHORT);
			}
			responeGetDaily = null;
		}

	}

	@Override
	public void hide(OnCompleteListener listener) {
		TraceView.instance.removeView(this.getName());
		getViewController().removeView(name);
	}

	@Override
	public void back() {
		if (viewDetail != null && viewDetail.onBack()) {
			return;
		}
		super.back();
		getViewController().removeView(getName());
	}

	class GetDailyLower implements HttpResponseListener {

		@Override
		public void handleHttpResponse(HttpResponse httpResponse) {
			responeGetDailyLower = (new JsonReader()).parse(httpResponse
					.getResultAsString());
		}

		@Override
		public void failed(Throwable t) {

		}

		@Override
		public void cancelled() {

		}
	}

	class CheckDaily implements HttpResponseListener {

		@Override
		public void handleHttpResponse(HttpResponse httpResponse) {
			responeCheck = (new JsonReader()).parse(httpResponse
					.getResultAsString());
		}

		@Override
		public void failed(Throwable t) {

		}

		@Override
		public void cancelled() {

		}
	}

	class GetDaily implements HttpResponseListener {

		@Override
		public void handleHttpResponse(HttpResponse httpResponse) {
			responeGetDaily = (new JsonReader()).parse(httpResponse
					.getResultAsString());
		}

		@Override
		public void failed(Throwable t) {

		}

		@Override
		public void cancelled() {

		}
	}

	class ViewDetail extends View {

		Table				tbContent;
		ScrollPane			scroll;
		Label[]				lbTitle;
		TextfieldStatic[]	lbInfo;
		String				agencyReceive;
		int					money;
		Image				bg;
		boolean				isSend	= false;

		ViewSendMoney		viewSendMoney;

		public ViewDetail() {
			setVisible(false);
			setSize(Constants.WIDTH_SCREEN, Constants.HEIGHT_SCREEN
					- Constants.HEIGHT_ACTIONBAR);
			this.top();
			bg = new Image(new NinePatch(Assets.instance.ui.reg_ninepatch,
					new Color(1, 1, 1, 1)));
			bg.setSize(getWidth(), getHeight());
			Label lbHeader = new Label("Thông tin đại lý", new LabelStyle(
					Assets.instance.fontFactory.getFont(30, fontType.Medium),
					Color.BLUE));
			lbTitle = new Label[12];
			lbInfo = new TextfieldStatic[10];

			String[] title = { "Tên đại lý", "Địa chỉ đại lý", "Cấp đại lý",
					"Số điện thoại đại lý", "Số điện thoại người giới thiệu",
					"Số tiền trong tài khoản", "Email", "Imei thiết bị",
					"Tên thiết bị", "Trạng thái", "Số tiền cần chuyển",
					"Ghi chú" };
			for (int i = 0; i < lbTitle.length; i++) {
				lbTitle[i] = new Label("", new LabelStyle(
						Assets.instance.fontFactory.getFont(20,
								fontType.Regular), Color.GRAY));
				lbTitle[i].setWrap(true);
				lbTitle[i].setWidth(180);
				lbTitle[i].setText(title[i]);
			}

			for (int i = 0; i < lbInfo.length; i++) {
				lbInfo[i] = new TextfieldStatic("", Color.BLACK, 270);
			}
			lbInfo[4].setHeight(lbTitle[4].getTextBounds().height + 10);
			lbInfo[4].setMinHeight(lbTitle[4].getTextBounds().height + 10);
			lbInfo[5].setHeight(lbTitle[5].getTextBounds().height + 10);
			lbInfo[5].setMinHeight(lbTitle[5].getTextBounds().height + 10);

			Table tbButton = new Table();
			TextButton btSend = new TextButton("Chuyển tiền",
					Style.ins.textButtonStyle);
			TextButton btCancel = new TextButton("Hủy",
					Style.ins.textButtonStyle);
			tbButton.add(btSend).width(120).height(40);
			tbButton.add(btCancel).padLeft(5).width(120).height(40);
			btSend.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					viewSendMoney.show(null);
					super.clicked(event, x, y);
				}
			});
			btCancel.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					super.clicked(event, x, y);
					hide(null);
					AbstractGameScreen.keyboard.hide();
					if (isSend) {
						isSend = false;
						Loading.ins.show(ViewCapTienDaiLy.this);
						Request.getInstance().getLowerDaily(
								AppPreference.instance.name,
								AppPreference.instance.pass, 1,
								new GetDailyLower());
					}
				}
			});

			tbContent = new Table();
			scroll = new ScrollPane(tbContent);

			this.addActor(bg);
			this.add(scroll);
			tbContent.add(lbHeader).padTop(10).colspan(3).padBottom(20).row();
			for (int i = 0; i < 10; i++) {
				tbContent.add(lbTitle[i]).width(180).padTop(5);
				tbContent.add(lbInfo[i]).padLeft(5).padTop(5).colspan(2).left()
						.row();
			}
			tbContent.add(tbButton).padTop(20).colspan(3);
			viewSendMoney = new ViewSendMoney();
			this.addActor(viewSendMoney);
		}

		void setInfo(String[] info) {
			for (int i = 0; i < lbInfo.length; i++) {
				lbInfo[i].setContent(info[i]);
				tbContent.getCell(lbInfo[i]).height(lbInfo[i].getHeight());
			}
			tbContent.invalidate();
		}

		@Override
		public void update(float delta) {
			viewSendMoney.update(delta);
		}

		@Override
		public void show(OnCompleteListener listener) {
			toFront();
			this.setViewState(ViewState.SHOW);
			setVisible(true);
			this.setPosition(getWidth(), 0);
			this.clearActions();
			this.addAction(Actions.moveTo(0, 0, 0.5f, Interpolation.exp10Out));
		}

		@Override
		public void hide(OnCompleteListener listener) {
			this.setViewState(ViewState.HIDE);
			this.addAction(Actions.sequence(
					Actions.moveTo(getWidth(), 0, 0.5f, Interpolation.exp10Out),
					Actions.visible(false)));
		}

		public boolean onBack() {
			if (viewSendMoney != null && viewSendMoney.onBack()) {
				return true;
			}
			if (ViewDetail.this.getViewState() == ViewState.SHOW) {
				hide(null);
				return true;
			}
			return false;
		}

		class ViewSendMoney extends View {
			JsonValue		responeSendMoney;
			TextfieldStatic	tfSMoney;
			CustomTextField	tfMoney;
			TextArea		taNote;

			public ViewSendMoney() {
				this.top();
				setVisible(false);
				setSize(Constants.WIDTH_SCREEN, Constants.HEIGHT_SCREEN
						- Constants.HEIGHT_ACTIONBAR);
				bg = new Image(new NinePatch(Assets.instance.ui.reg_ninepatch,
						new Color(1, 1, 1, 1)));
				bg.setSize(getWidth(), getHeight());

				Label lbHeader = new Label("Chuyển tiền",
						new LabelStyle(Assets.instance.fontFactory.getFont(30,
								fontType.Medium), Color.BLUE));
				Label lbMoney = new Label("Số tiền còn lại", new LabelStyle(
						Assets.instance.fontFactory.getFont(20,
								fontType.Regular), Color.GRAY));
				Label lbMoneySend = new Label("Số tiền cấn chuyển",
						new LabelStyle(Assets.instance.fontFactory.getFont(20,
								fontType.Regular), Color.GRAY));
				Label lbNote = new Label("Ghi chú", new LabelStyle(
						Assets.instance.fontFactory.getFont(20,
								fontType.Regular), Color.GRAY));

				tfSMoney = new TextfieldStatic(UserInfo.money + " "
						+ UserInfo.currency, Color.BLACK, 270);

				tfMoney = new CustomTextField("",
						Style.ins.getTextFieldStyle(8,
								Assets.instance.fontFactory.getFont(25,
										fontType.Light)));
				tfMoney.setOnscreenKeyboard(AbstractGameScreen.keyboard);
				tfMoney.addListener(new InputListener() {
					@Override
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						AbstractGameScreen.keyboard.registerTextField(tfMoney,
								KeyboardConfig.NUMBER,
								KeyboardConfig.SINGLE_LINE);
						return super.touchDown(event, x, y, pointer, button);
					}
				});

				Label lbCurrency = new Label(UserInfo.currency, new LabelStyle(
						Assets.instance.fontFactory.getFont(20,
								fontType.Regular), Color.BLACK));

				taNote = new TextArea("",
						Style.ins.getTextFieldStyle(8,
								Assets.instance.fontFactory.getFont(25,
										fontType.Light)));
				taNote.setOnscreenKeyboard(AbstractGameScreen.keyboard);
				taNote.addListener(new InputListener() {
					@Override
					public boolean touchDown(InputEvent event, float x,
							float y, int pointer, int button) {
						AbstractGameScreen.keyboard.registerTextField(taNote,
								KeyboardConfig.NORMAL,
								KeyboardConfig.MULTI_LINE);
						return super.touchDown(event, x, y, pointer, button);
					}
				});

				Table tbButton = new Table();
				TextButton btSend = new TextButton("Ok",
						Style.ins.textButtonStyle);
				TextButton btCancel = new TextButton("Hủy",
						Style.ins.textButtonStyle);
				tbButton.add(btSend).width(120).height(40);
				tbButton.add(btCancel).padLeft(5).width(120).height(40);
				btSend.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						super.clicked(event, x, y);
						if (!Factory.isNumeric(tfMoney.getText())) {
							Toast.makeText(getStage(),
									"Vui lòng nhập đúng số tiền",
									Toast.LENGTH_SHORT);
						} else if (StringUtil.isContainSpecialChar(taNote
								.getText())) {
							Toast.makeText(getStage(),
									"Ghi chú không được chứa ký tự đặc biệt",
									Toast.LENGTH_SHORT);
						} else {
							if (Integer.parseInt(tfMoney.getText()) > UserInfo.money) {
								Toast.makeText(
										getStage(),
										"Số tiền chuyển phải nhỏ hơn số tiền của bạn",
										Toast.LENGTH_SHORT);
							} else {
								AbstractGameScreen.keyboard.hide();
								DialogCustom dl = new DialogCustom("");
								dl.text("Bạn có chắc chắn muốn chuyển "
										+ tfMoney.getText() + " "
										+ UserInfo.currency + " cho "
										+ agencyReceive);
								dl.button("Ok", new Runnable() {
									@Override
									public void run() {
										Loading.ins.show(ViewDetail.this);
										Request.getInstance().sendMoney(
												AppPreference.instance.name,
												AppPreference.instance.pass,
												agencyReceive,
												tfMoney.getText(),
												UserInfo.currency,
												taNote.getText(),
												new SendMoney());
									}

								});
								dl.button("Hủy", new Runnable() {
									@Override
									public void run() {

									}
								});
								dl.show(getStage());
							}
						}
					}
				});
				btCancel.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						super.clicked(event, x, y);
						hide(null);
						AbstractGameScreen.keyboard.hide();
					}
				});

				this.addActor(bg);
				this.add(lbHeader).padTop(10).colspan(3).row();
				this.add(lbMoney).width(180).padTop(15);
				this.add(tfSMoney).padTop(15).colspan(2).left().row();
				this.add(lbMoneySend).width(180).padTop(5);
				this.add(tfMoney).left().width(200).padTop(5).height(35);
				this.add(lbCurrency).left().padTop(5).row();
				this.add(lbNote).width(180).padTop(5);
				this.add(taNote).width(270).padTop(5).height(100).left()
						.colspan(2).row();
				this.add(tbButton).padTop(20).colspan(3);
			}

			@Override
			public void update(float delta) {
				if (responeSendMoney != null) {
					Loading.ins.hide();
					boolean result = responeSendMoney
							.getBoolean(ExtParamsKey.RESULT);
					if (result) {
						isSend = true;
						int moneyTrans = responeSendMoney
								.getInt(ExtParamsKey.MONEY_TRANSFER);
						money += moneyTrans;
						lbInfo[5].setContent(money + " " + UserInfo.currency);
						UserInfo.money = responeSendMoney
								.getInt(ExtParamsKey.MONEY_UPDATE);
						tfSMoney.setContent(UserInfo.money + " "
								+ UserInfo.currency);
					}
					String mess = responeSendMoney
							.getString(ExtParamsKey.MESSAGE);
					Toast.makeText(getStage(), mess, Toast.LENGTH_SHORT);
					responeSendMoney = null;
				}
			}

			@Override
			public void show(OnCompleteListener listener) {
				toFront();
				setViewState(ViewState.SHOW);
				setVisible(true);
				tfMoney.setText("");
				taNote.setText("");
				this.setPosition(getWidth(), 0);
				this.clearActions();
				this.addAction(Actions.moveTo(0, 0, 0.5f,
						Interpolation.exp10Out));
			}

			@Override
			public void hide(OnCompleteListener listener) {
				setViewState(ViewState.HIDE);
				this.addAction(Actions.sequence(Actions.moveTo(getWidth(), 0,
						0.5f, Interpolation.exp10Out), Actions.visible(false)));
			}

			class SendMoney implements HttpResponseListener {

				@Override
				public void handleHttpResponse(HttpResponse httpResponse) {
					responeSendMoney = (new JsonReader()).parse(httpResponse
							.getResultAsString());
				}

				@Override
				public void failed(Throwable t) {
				}

				@Override
				public void cancelled() {
				}

			}

			public boolean onBack() {
				if (ViewSendMoney.this.getViewState() == ViewState.SHOW) {
					hide(null);
					return true;
				}
				return false;
			}

		}

	}

}

package com.coder5560.game.screens;

import utils.factory.Log;
import utils.factory.StringSystem;
import utils.screen.AbstractGameScreen;
import utils.screen.GameCore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.coder5560.game.enums.Constants;
import com.coder5560.game.enums.ViewState;
import com.coder5560.game.listener.OnSelectListener;
import com.coder5560.game.views.TraceView;
import com.coder5560.game.views.ViewController;

public class GameScreen extends AbstractGameScreen {
	ViewController			controller;
	Image					flash;
	public GestureDetector	gestureDetector;

	public GameScreen(GameCore game) {
		super(game);
	}

	@Override
	public void show() {
		super.show();

		controller = new ViewController(parent, this);
		controller.setFacebookConnector(parent.facebookConnector);
		controller.platformResolver = parent.getPlatformResolver();
		controller.build(stage);
		try {
			Constants.DEVICE_ID = controller.platformResolver.getDeviceID();
			Constants.DEVICE_NAME = controller.platformResolver.getDeviceName();
			Log.d("Device_id=" + Constants.DEVICE_ID);
			Log.d("Device_name=" + Constants.DEVICE_NAME);
		} catch (Exception e) {

		}
		Gdx.input.setCatchBackKey(true);
		System.gc();

	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void update(float delta) {
		controller.update(delta);
		if (isExit) {
			timeExit += delta;
			if (timeExit >= 2) {
				timeExit = 0;
				isExit = false;
			}
		}
	}

	boolean	isExit		= false;
	float	timeExit	= 0;

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.BACK || keycode == Keys.ESCAPE) {

			if (controller.getView(StringSystem.VIEW_MAIN_MENU).getViewState() == ViewState.SHOW) {
				controller.getView(StringSystem.VIEW_MAIN_MENU).hide(null);
				return true;
			}

			if (AbstractGameScreen.keyboard.isShowing()) {
				AbstractGameScreen.keyboard.hide();
				return true;
			}
			TraceView.instance.debug();
			if (controller.getView(TraceView.instance.getLastView()) != null)
				controller.getView(TraceView.instance.getLastView()).back();
		}
		return false;
	}

	public void setGestureDetector(GestureDetector detector) {
		this.gestureDetector = detector;
		parent.inputMultiplexer = new InputMultiplexer(keyboard, detector,
				this, stage);
		Gdx.input.setInputProcessor(parent.inputMultiplexer);
	}

	OnSelectListener	selectListener	= new OnSelectListener() {

											@Override
											public void onSelect(int i) {
											}
										};

}
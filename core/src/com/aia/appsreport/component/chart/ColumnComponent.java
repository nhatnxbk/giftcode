package com.aia.appsreport.component.chart;

import utils.factory.StringUtil;
import utils.factory.FontFactory.fontType;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.coder5560.game.assets.Assets;

public class ColumnComponent extends Group {
	LabelStyle style;
	Image bg;
	Label lbvalue;
	float value;
	int index = -1;

	public ColumnComponent(int value, Color color) {
		this.value = value;
		style = new LabelStyle(Assets.instance.fontFactory.getFont(20,
				fontType.Regular), Color.BLACK);
		bg = new Image(new NinePatch(new TextureRegion(new Texture(
				Gdx.files.internal("Img/ninepatch_vien.png"))), 1, 1, 1, 1));
		bg.setColor(color);
		lbvalue = new Label(value + "", style);
		addActor(bg);
		addActor(lbvalue);
		bg.setPosition(getX(), getY());
		if (value == 0) {
			lbvalue.setText("");
		} else {
			String strValue = StringUtil.getStrMoney(value);
			lbvalue.setText(strValue);
		}
	}

	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);
		bg.setSize(width, height);
	}

	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		lbvalue.setPosition(
				bg.getX() + bg.getWidth() / 2 - lbvalue.getTextBounds().width
						/ 2,
				bg.getY() + bg.getHeight() / 2 - lbvalue.getHeight() / 2);
	}
}

package com.tankbattle.game.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.tankbattle.game.MyGdxGame;

/**
 * Created by Aditya Hadi on 11/13/2016.
 */

public class RedWall extends GameObjects {
    private TiledMapTileLayer map;
    public TextureRegion objecTextureRegion;

    public RedWall(MyGdxGame app){
        application = app;
        objecTextureRegion = TextureRegion.split(gameSpriteSheet, 16, 16)[0][16]; // destructible wall
        setBounds(0, 0, objecTextureRegion.getRegionWidth(), objecTextureRegion.getRegionHeight());
    }

    @Override
    public void act(float deltaTime){
        super.act(deltaTime);
        stateTime += deltaTime;

    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        if(!dispose)
            batch.draw(objecTextureRegion, getX(), getY());
        else
            disposeAnimation(batch);
    }
}

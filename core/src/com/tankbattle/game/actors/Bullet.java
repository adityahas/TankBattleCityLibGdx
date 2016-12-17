package com.tankbattle.game.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by Aditya Hadi on 11/13/2016.
 */

public class Bullet extends GameObjects {
    private TiledMapTileLayer map;
    public TextureRegion bulletTextureRegion;
    public float direction;
    public float speed = 2.5f;
    private Tank tank;

    public Bullet(float rotation, float x, float y, Tank tank) {
        this.tank = tank;
        direction = rotation;
        bulletTextureRegion = TextureRegion.split(gameSpriteSheet, 16, 16)[5][20]; // bullet
        setBounds(x, y, bulletTextureRegion.getRegionWidth(), bulletTextureRegion.getRegionHeight());
        setRotation(direction);
    }

    @Override
    public void act(float deltaTime){
        super.act(deltaTime);
        stateTime += deltaTime;

        if(!dispose) {
            if (direction == 180)
                moveBy(0, -speed);
            if (direction == 0)
                moveBy(0, speed);
            if (direction == 90)
                moveBy(-speed, 0);
            if (direction == -90)
                moveBy(speed, 0);
        }

        setCellX(getWorldToCellX(), false);
        setCellY(getWorldToCellY(), false);
        Actor collidingActor;
        if(!dispose)
            collidingActor = getCollisionActor(getX(), getY(), getWidth()/2, getHeight()/2);
        else
            collidingActor = null;

        if (collidingActor instanceof RedWall) {
            ((RedWall) collidingActor).removeFromMapArray(((RedWall) collidingActor).getCellX(), ((RedWall) collidingActor).getCellY());
            collidingActor.remove();
            dispose();
        }
        if (collidingActor instanceof SilverWall) {
            dispose();
        }
        if (collidingActor instanceof Base) {
            ((Base)collidingActor).reduceHP();
            dispose();
        }
        if (collidingActor instanceof Tank) {
            if(collidingActor != this.tank)
            {
                ((Tank)collidingActor).reduceHP();
                dispose();
            }
        }

        if (getX() < 0 || getX() > getStage().getWidth() || getY() < 0 || getX() > getStage().getHeight()) {
            remove();
            return;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha){
        if(!dispose)
            batch.draw(bulletTextureRegion, getX(), getY(), getWidth()/2, getHeight()/2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation() );
        else
            disposeAnimation(batch);
    }
}

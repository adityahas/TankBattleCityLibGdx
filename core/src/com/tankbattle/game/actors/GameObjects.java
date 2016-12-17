package com.tankbattle.game.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.tankbattle.game.MyGdxGame;

/**
 * Created by Aditya Hadi on 11/13/2016.
 */

public class GameObjects extends Actor {
    protected Texture gameSpriteSheet = new Texture("data/maps/tiled/tankbattlesprite.png");
    protected Animation disposeAnim;
    protected boolean dispose = false;
    protected float stateTime = 0;
    protected int posCellX = 0;
    protected int posCellY = 0;
    protected final byte TILE_WIDTH = 16;
    protected final byte TILE_HEIGHT = 16;
    protected MyGdxGame application;
    protected int clientID;
    protected int baseID = 0;
    private int actorHP = 5;

    @Override
    public void act(float delta) {
        super.act(delta);
        if (actorHP <= 0) {
            removeFromMapArray(getCellX(), getCellY());
            clear();
            remove();
        }
    }

    // get actor cell location
    public int getCellX() {
        return posCellX;
    }

    public int getCellY() {
        return posCellY;
    }

    public int getWorldToCellX() {
        return (byte) (getX() / TILE_WIDTH);
    }

    public int getWorldToCellY() {
        return (byte) (getY() / TILE_HEIGHT);
    }

    public float getCellToWorldX() {
        return posCellX * (float) TILE_WIDTH;
    }

    public float getCellToWorldY() {
        return posCellY * (float) TILE_HEIGHT;
    }

    public void setCellX(int x, boolean updateRealPos) {
        posCellX = x;
        if (updateRealPos)
            setX(getCellToWorldX());
    }

    public void setCellY(int y, boolean updateRealPos) {
        posCellY = y;
        if (updateRealPos)
            setY(getCellToWorldY());
    }

    public void setCellX(int x) {
        posCellX = x;
        setX(getCellToWorldX());
    }

    public void setCellY(int y) {
        posCellY = y;
        setY(getCellToWorldY());
    }

    public void initPositionUsingCellX(int cellX) {
        setCellX(cellX, true);
    }

    public void initPositionUsingCellY(int cellY) {
        setCellY(cellY, true);
    }

    public Actor getCollisionActor() {
        return getCollisionActor(getX(), getY(), getWidth(), getHeight());
    }

    public Actor getCollisionActor(float x, float y, float width, float height) {

        Array<Actor> actors = getStage().getActors();
        Rectangle actorRect = new Rectangle(x, y, width, height);
        Rectangle actorRect2 = new Rectangle();

        int i = 0;
        while (actors.size > i) {
            actorRect2.set(actors.get(i).getX(), actors.get(i).getY(), actors.get(i).getWidth(), actors.get(i).getHeight());
            if (Intersector.overlaps(actorRect, actorRect2) && actors.get(i) != this) {
                return actors.get(i);
            }
            i++;
        }

        return null;
    }

    public void dispose() {
        dispose = true;
        TextureRegion[][] ss = TextureRegion.split(gameSpriteSheet, 16, 16);
        disposeAnim = new Animation(0.33f, ss[8][16], ss[8][17], ss[8][18]);
        disposeAnim.setPlayMode(Animation.PlayMode.NORMAL);
    }

    public void disposeAnimation(Batch batch) {
        TextureRegion frame = disposeAnim.getKeyFrame(stateTime);
        batch.draw(frame, getCellToWorldX(), getCellToWorldY());

        if (disposeAnim.isAnimationFinished(stateTime)) {
            remove();
        }
    }

    public void snapPosition() {
        setX(getCellToWorldX());
        setY(getCellToWorldY());
    }


    public boolean isTankCollidingBlockade() {
        return isTankCollidingBlockade(getX(), getY(), getWidth(), getHeight());
    }

    public boolean isTankCollidingBlockade(float x, float y, float width, float height) {
        Actor collidingActor = getCollisionActor(x, y, width, height);
        if (collidingActor != null && (
                collidingActor instanceof RedWall ||
                        collidingActor instanceof SilverWall ||
                        collidingActor instanceof Base ||
                        collidingActor instanceof Water ||
                        collidingActor instanceof Tank
        )) {
            return true;
        }
        return false;
    }

    public void removeFromMapArray(int posCellX, int posCellY) {
        actorHP = 0;
        application.setMapArray(posCellX, posCellY, 0);
    }

    public void reduceHP() {
        Gdx.app.log("gameplay", "reduce HP");
        actorHP -= 1;

        if (actorHP <= 0) {
            if (this instanceof Tank) {
                Gdx.app.log("gameplay", "tank destroyed clientID : " + clientID);
                application.updateScore(1 - clientID);
            } else if (this instanceof Base) {
                Gdx.app.log("gameplay", "base destroyed baseID : " + baseID);
                application.updateScore(1 - baseID);
            }
        }
    }

    public int getActorHP() {
        return actorHP;
    }

    public boolean isAlive() {
        if (actorHP > 0)
            return true;

        return false;
    }

    public boolean isDead() {
        if (actorHP <= 0)
            return true;

        return false;
    }
}

package com.tankbattle.game.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.tankbattle.game.MyGdxGame;

/**
 * Created by Aditya Hadi on 11/13/2016.
 */

public class Tank extends GameObjects {
    private Animation tankAnimationIdle;
    private Animation tankAnimationWalk;
    private TextureRegion tankTextureRegion[][];
    private float speed;
    private boolean actionShoot = false;
    private boolean actionMove = false;
    private int[] actionMoveInfo = new int[2];
    private float actionAllowedInterval = 1f;
    private boolean actionAllowed;
    private Base base;

    public Tank(int cell_x, int cell_y, MyGdxGame app){
        application = app;
        speed = 2.5f;
        tankTextureRegion = TextureRegion.split(gameSpriteSheet, 16, 16);
        tankAnimationIdle = new Animation(0, tankTextureRegion[0][0]);
        tankAnimationWalk = new Animation(0.15f, tankTextureRegion[0][0], tankTextureRegion[0][1]);
        tankAnimationIdle.setPlayMode(Animation.PlayMode.LOOP);
        tankAnimationWalk.setPlayMode(Animation.PlayMode.LOOP);
        setCellX(cell_x);
        setCellY(cell_y);
        setBounds(getX(), getY(), tankTextureRegion[0][0].getRegionWidth(), tankTextureRegion[0][0].getRegionHeight());
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
        if(clientID == 0){
            tankTextureRegion = TextureRegion.split(gameSpriteSheet, 16, 16);
            tankAnimationIdle = new Animation(0, tankTextureRegion[8][8]);
            tankAnimationWalk = new Animation(0.15f, tankTextureRegion[8][8], tankTextureRegion[8][9]);
            tankAnimationIdle.setPlayMode(Animation.PlayMode.LOOP);
            tankAnimationWalk.setPlayMode(Animation.PlayMode.LOOP);
        }
    }

    public int getClientID() {
        return clientID;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        if(!dispose){
            TextureRegion frame;
            if(hasActions())
                frame = tankAnimationWalk.getKeyFrame(stateTime);
            else
                frame = tankAnimationIdle.getKeyFrame(stateTime);
            batch.draw(frame, getX(), getY(), getWidth()/2, getHeight()/2, getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation() );
        }
        else
            disposeAnimation(batch);
    }

    @Override
    public void act(float deltaTime){
        super.act(deltaTime);
        stateTime += deltaTime;

        if(getActorHP() > 0){
            actionAllowed = false;
            actionAllowedInterval -= deltaTime;
            if(actionAllowedInterval < 0){
                actionAllowed = true;
            }

            if (isTankCollidingBlockade()) {
                clearActions();
            }


            if(actionShoot && actionAllowed){
                execShoot();
                resetShoot();
            }

            if(actionMove && actionAllowed){
                execMoveByCell(actionMoveInfo[0], actionMoveInfo[1]);

                if(getActions().size == 0){
                    setCellX(getCellX());
                    setCellY(getCellY());
                }
                resetMoveByCell();
            }

            if(actionAllowed)
                resetActionAllowedInterval();
        }
    }

    public void shoot(){
        actionShoot = true;
    }

    private void execShoot(){
        Bullet bullet = new Bullet(getRotation(), getX(), getY(), this);
        bullet.setCellX(getWorldToCellX());
        bullet.setCellY(getWorldToCellY());
        getStage().addActor(bullet);
    }

    public void execMoveByCell(int add_x, int add_y){
        // rotate tank direction
        if(add_y > 0)
            setRotation(0);
        if(add_y < 0)
            setRotation(180);
        if(add_x < 0)
            setRotation(90);
        if(add_x > 0)
            setRotation(-90);

        // check if it's ok to move
        if(!isTankCollidingBlockade(
                getCellToWorldX() + (float)(add_x * TILE_WIDTH),
                getCellToWorldY() + (float)(add_y * TILE_HEIGHT),
                TILE_WIDTH,
                TILE_HEIGHT
        ))
        {
            setCellX(getCellX() + add_x, false);
            setCellY(getCellY() + add_y, false);
            Gdx.app.log("GP","move execute "+clientID+" new pos "+getCellX()+","+getCellY());

            // add move action
            MoveToAction moveAction = new MoveToAction();
            moveAction.setPosition(getCellToWorldX(), getCellToWorldY());
            moveAction.setDuration(1f);
            addAction(moveAction);
        }
    }
    private void resetShoot(){
        actionShoot = false;
    }

    public void moveByCell(int add_x, int add_y){
        actionMove = true;
        actionMoveInfo[0] = add_x;
        actionMoveInfo[1] = add_y;
    }

    private void resetMoveByCell(){
        actionMove = false;
        actionMoveInfo[0] = 0;
        actionMoveInfo[1] = 0;
    }

    private void resetActionAllowedInterval(){
        actionAllowedInterval = 1f;
    }

    public boolean isActionAllowed() {
        return actionAllowed;
    }

    public Base getBase() {
        return base;
    }

    public void setBase(Base base) {
        this.base = base;
    }


    public int getNumberOfActorAlive(){
        int n = 0;
        if(getActorHP() > 0)
            n += 1;
        if(getBase().getActorHP() > 0)
            n += 1;

        return n;
    }
}

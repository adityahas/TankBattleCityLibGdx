package com.tankbattle.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tankbattle.game.actors.Base;
import com.tankbattle.game.actors.GameObjects;
import com.tankbattle.game.actors.RedWall;
import com.tankbattle.game.actors.SilverWall;
import com.tankbattle.game.actors.Tank;
import com.tankbattle.game.actors.Water;
import com.tankbattle.game.utils.Define;
import com.tankbattle.game.utils.Server;

import java.util.Arrays;

public class MyGdxGame extends ApplicationAdapter {
    final int STATE_NOPLAYER = 0;
    final int STATE_ACTIVE = 1;

    private TiledMap tiledMap;
    private byte[] mapArray;
    private final float _tileWidth = 16f;
    private final float _tileHeight = 16f;
    private OrthogonalTiledMapRenderer tileMapRenderer;
    private BitmapFont font;
    private SpriteBatch batch;
    private Viewport viewport;
    private TiledMapTileLayer mainLayer;
    private Tank tankPlayer0;
    private Tank tankPlayer1;
    private GameObjects[] mapObjects;
    private int numberOfMapObjects;
    private Stage stage;
    private Server server;
    private boolean gameStarted = false;
    public String playerName[];
    private int playerState[];
    private Skin skin;
    private TextButton playBtn;
    private Window optionsWindow;
    private Label titleText;
    private Label playerText1;
    private Label playerText2;
    private Label winnerLabel;
    private float battleDuration = 5f * 60f;
    private boolean timeUp;
    private int[] battleScore;
    private Label timerText;

    @Override
    public void create() {
        playerName = new String[2];
        playerState = new int[2];
        playerName[0] = "NONE";
        playerName[1] = "NONE";

        // init battle tiledMap
        tiledMap = new TmxMapLoader().load("data/maps/tiled/level1.tmx");
        mainLayer = (TiledMapTileLayer) tiledMap.getLayers().get(0);
        tileMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, 1f);

        // init stage for putting actors
        viewport = new ExtendViewport(mainLayer.getWidth() * _tileWidth, mainLayer.getHeight() * _tileHeight);
        stage = new Stage(viewport);

        // init actors
        createActors();
        // init game UI (need to init stage first because the UI will be putted on stage
        initUI();

        // init the batch for painting the whole game
        batch = new SpriteBatch();

        // init server
        server = new Server(this);
        server.start();

        battleScore = new int[]{0, 0};
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // get the delta time
        float deltaTime = Gdx.graphics.getDeltaTime();

        tileMapRenderer.setView((OrthographicCamera) stage.getCamera());
        tileMapRenderer.render();
        stage.act(deltaTime);
        gameUpdate();
        stage.draw();

        playerText1.setText("Player 1 : " + playerName[0]);
        playerText2.setText("Player 2 : " + playerName[1]);
    }

    private void gameUpdate() {
        // update gameplay
        if (gameStarted) {

            overrideTankInput(0);
//			overrideTankInput(1);

            battleDuration -= Gdx.graphics.getDeltaTime();
            timerText.setText("" + ((int) (battleDuration)));

            if (battleDuration < 0) {
                timeUp = true;
            }

            switch (checkBattleEnd()) {
                case 1:
                    winnerLabel.setText(playerName[0] + "\nScore : " + battleScore[0]);
                    showBattleResultScreen();
                    gameStarted = false;
                    break;
                case 2:
                    winnerLabel.setText(playerName[1] + "\nScore : " + battleScore[1]);
                    showBattleResultScreen();
                    gameStarted = false;
                    break;
                case 3:
                    winnerLabel.setText("Battle Draw!!!");
                    showBattleResultScreen();
                    gameStarted = false;
                    break;
            }
        }
    }

    private void showBattleResultScreen() {
        optionsWindow.setVisible(true);
    }

    /**
     * @return 0 = battle in progress, 1 = tank1 win, 2 = tank2 win, 3 = draw
     */
    private int checkBattleEnd() {
        int winner;

        if (tankPlayer0.getNumberOfActorAlive() > tankPlayer1.getNumberOfActorAlive()) {
            winner = 1;
        } else if (tankPlayer0.getNumberOfActorAlive() < tankPlayer1.getNumberOfActorAlive()) {
            winner = 2;
        } else {
            winner = 3;
        }

        if (timeUp) {
            return winner;
        } else if (tankPlayer0.isDead() && tankPlayer1.isDead()) {
            battleDuration = 0;
            return winner;
        } else if (tankPlayer0.getNumberOfActorAlive() == 0 || tankPlayer1.getNumberOfActorAlive() == 0) {
            battleDuration = 0;
            return winner;
        }
        return 0;
    }

    private void overrideTankInput(int clientID) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP))
            move(clientID, Define.DIR_UP);
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT))
            move(clientID, Define.DIR_RIGHT);
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN))
            move(clientID, Define.DIR_DOWN);
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT))
            move(clientID, Define.DIR_LEFT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            shoot(clientID, (int) tankPlayer0.getRotation());
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("screen", "width:" + width + " height:" + height);
        stage.getViewport().update(width, height, false);
    }

    @Override
    public void dispose() {
        tiledMap.dispose();
    }

    private void createActors() {
//		tankPlayer0 = new Tank(2, 1, this);
        tankPlayer0 = new Tank(28, 28, this);
        tankPlayer0.setClientID(0);
        tankPlayer0.setRotation(180f);

        tankPlayer1 = new Tank(1, 1, this);
        tankPlayer1.setClientID(1);

        stage.addActor(tankPlayer0);
        stage.addActor(tankPlayer1);

        mapArray = new byte[mainLayer.getWidth() * mainLayer.getHeight()];
        int mapArrayCounter = 0;

        // generate all tiledMap objects
        mapObjects = new GameObjects[mainLayer.getWidth() * mainLayer.getHeight()];
        numberOfMapObjects = 0;
        debugMapIndex();
        boolean tileBase1Found = false;
        for (int y = mainLayer.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < mainLayer.getWidth(); x++) {

                mapArray[mapArrayCounter] = Define.TILE_EMPTY;

                if (mainLayer.getCell(x, y) != null) {

                    int objID = mainLayer.getCell(x, y).getTile().getId();

                    if (objID == 17 || objID == 42 || objID == 92 || objID == 70) // 267 = empty
                    {

                        if (objID == 17) // red wall
                        {
                            mapObjects[numberOfMapObjects] = new RedWall(this);
                            mapArray[mapArrayCounter] = Define.TILE_RED_WALL;
                        }
                        if (objID == 42) // silver wall
                        {
                            mapObjects[numberOfMapObjects] = new SilverWall();
                            mapArray[mapArrayCounter] = Define.TILE_SILVER_WALL;
                        }
                        if (objID == 92) // water
                        {
                            mapObjects[numberOfMapObjects] = new Water();
                            mapArray[mapArrayCounter] = Define.TILE_WATER;
                        }
                        if (objID == 70) // base
                        {
                            if (!tileBase1Found) {
                                tileBase1Found = true;
                                mapArray[mapArrayCounter] = Define.TILE_INIT_BASE;
                                mapObjects[numberOfMapObjects] = new Base(0, this);
                                mapObjects[numberOfMapObjects].initPositionUsingCellX(x);
                                mapObjects[numberOfMapObjects].initPositionUsingCellY(y);
                                tankPlayer0.setBase((Base) mapObjects[numberOfMapObjects]);
                            } else {
                                mapArray[mapArrayCounter] = Define.TILE_INIT_BASE;
                                mapObjects[numberOfMapObjects] = new Base(1, this);
                                mapObjects[numberOfMapObjects].initPositionUsingCellX(x);
                                mapObjects[numberOfMapObjects].initPositionUsingCellY(y);
                                tankPlayer1.setBase((Base) mapObjects[numberOfMapObjects]);
                            }
                        }

                        mapObjects[numberOfMapObjects].initPositionUsingCellX(x);
                        mapObjects[numberOfMapObjects].initPositionUsingCellY(y);

                        stage.addActor(mapObjects[numberOfMapObjects]);

                        // clear the layer of current tile
                        mainLayer.setCell(x, y, new TiledMapTileLayer.Cell());
                        numberOfMapObjects++;
                    }
                }
                mapArrayCounter++;
            }
        }
        Gdx.input.setInputProcessor(stage);
    }

    private void debugMapIndex() {
        for (int y = mainLayer.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < mainLayer.getWidth(); x++) {
                if (mainLayer.getCell(x, y) != null) {
                    System.out.print(mainLayer.getCell(x, y).getTile().getId() + "\t");
                } else
                    System.out.print(0 + "\t");
            }
            System.out.println();
        }
    }

    private void debugGetMap(byte[] mapArrayForClient) {
        byte[][] mapData = new byte[Define.TILEMAP_HEIGHT][Define.TILEMAP_WIDTH];

        for (int i = 0; i < mapArrayForClient.length; i++) {
            mapData[i / Define.TILEMAP_HEIGHT][i % Define.TILEMAP_WIDTH] = mapArrayForClient[i];
//				mapData[i%Define.TILEMAP_WIDTH][i/Define.TILEMAP_HEIGHT] = mapArrayForClient[i];
        }

        for (int y = mainLayer.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < mainLayer.getWidth(); x++) {
                System.out.print(mapData[y][x] + "\t");
            }
            System.out.println();
        }
    }

    public synchronized void registerNewPlayer(int player, String name) {
        if (playerState[player] == STATE_NOPLAYER) {
            playerState[player] = STATE_ACTIVE;
            playerName[player] = name;
        } else {
            System.out.println("GAME : Player : " + name + " can not be added");
            System.out.println("GAME : Player already active at id : " + player);
        }
    }

    private void initUI() {
        font = new BitmapFont();
        skin = new Skin(Gdx.files.internal("data/ui/ui.skin"));

        initControlWindow();
        initBattleResultScreen();

    }

    private void initControlWindow() {
        float tableWidth = 170f;

        playBtn = new TextButton("   START   ", skin, "mainmenu");
        playBtn.setTransform(true);
        playBtn.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (!gameStarted) {
                    gameStarted = true;
//					playBtn.setText("STARTED...");
                    playBtn.setTouchable(Touchable.disabled);
                }
            }
        });

        titleText = new Label("GAMELOFT \nTANK BATTLE \nCITY!", skin, "dialogtitle");
        titleText.setWrap(true);
        Label timerTextTitle = new Label("Timer :", skin, "dialogtitle");
        timerText = new Label("", skin, "dialogtitle");
        timerText.setText("" + ((int) (battleDuration)));
        timerText.setFontScale(3f);
        timerText.setColor(Color.GOLD);
        playerText1 = new Label("Player 1 : none", skin, "paragraph");
        playerText1.setWrap(true);
        playerText2 = new Label("Player 2 : none", skin, "paragraph");
        playerText2.setWrap(true);

        Table tableControl = new Table();
        tableControl.setWidth(tableWidth);
        tableControl.row().center().fillX();
        tableControl.add(timerTextTitle);
        tableControl.row().center().fillX();
        tableControl.add(timerText);
        tableControl.row().center().fillX();
        tableControl.add(titleText);
        tableControl.row().center().fillX();
        tableControl.add(playBtn);
        tableControl.row().center().fillX();
        tableControl.add(playerText1);
        tableControl.row().center().fillX();
        tableControl.add(playerText2);
        tableControl.setPosition(stage.getViewport().getWorldWidth() - tableWidth, stage.getViewport().getWorldHeight() / 2);
        tableControl.setVisible(true);

        stage.addActor(tableControl);
    }

    private void initBattleResultScreen() {
        optionsWindow = new Window("", skin, "options");
        float winWidth = 200f;
        float winHeight = 200f;
        optionsWindow.setSize(winWidth, winHeight);
        optionsWindow.setPosition((stage.getViewport().getWorldWidth() - winWidth) / 2, (stage.getViewport().getWorldHeight() - winHeight) / 2);

        Label titleLable = new Label("Battle Ended :", skin, "dialogtitle_black");
        winnerLabel = new Label("Your Name", skin, "dialogtitle_red");

        optionsWindow.row().pad(10f).center().uniformX();
        optionsWindow.add(titleLable);
        optionsWindow.row();
        optionsWindow.add(winnerLabel);
        optionsWindow.setVisible(false);

        stage.addActor(optionsWindow);
    }

    public byte[] getCurrentPosition(int clientID) {
        if (gameStarted) {
            byte[] pos = new byte[3];
            pos[0] = Define.SOCK_REQ_GET_CURRENT_POSITOIN;
            Tank tankObj;
            if (clientID == 0)
                tankObj = tankPlayer0;
            else
                tankObj = tankPlayer1;

            if (tankObj.getActorHP() > 0) {
                pos[1] = (byte) tankObj.getCellX();
                pos[2] = (byte) tankObj.getCellY();
            } else {
                pos[1] = Define.TILEMAP_WIDTH + 1; // don't put zero since when parsing to 1d array will be fail
                pos[2] = Define.TILEMAP_HEIGHT + 1; // don't put zero since when parsing to 1d array will be fail
            }

            return pos;
        }

        return null;
    }

    private byte[] getBasePosition(int clientID) {
        if (gameStarted) {
            byte[] pos = new byte[3];
            pos[0] = Define.SOCK_REQ_GET_BASE_POSITOIN;
            Tank tankObj;
            if (clientID == 0)
                tankObj = tankPlayer0;
            else
                tankObj = tankPlayer1;

            pos[1] = (byte) tankObj.getBase().getCellX();
            pos[2] = (byte) tankObj.getBase().getCellY();

            return pos;
        }

        return null;
    }

    public byte[] getMap(int clientID) {
        if (gameStarted) {

            byte[] currentOwnTankPosition = getCurrentPosition(clientID); // own tank
            byte[] currentEnemyTankPosition = getCurrentPosition(1 - clientID); // enemy tank
            byte[] ownBasePos = getBasePosition(clientID);
            byte[] enemyBasePos = getBasePosition(1 - clientID);

            byte[] mapArrayForClient = Arrays.copyOf(mapArray, mapArray.length);
            // update tanks location on mapArray
            for (int i = 0; i < mapArray.length; i++) {
                if (currentOwnTankPosition[1] + ((Define.TILEMAP_HEIGHT_MAX_VAL - currentOwnTankPosition[2]) * Define.TILEMAP_HEIGHT) == i)
                    mapArrayForClient[i] = Define.TILE_OWN_TANK;
                if (currentEnemyTankPosition[1] + ((Define.TILEMAP_HEIGHT_MAX_VAL - currentEnemyTankPosition[2]) * Define.TILEMAP_HEIGHT) == i) {
                    mapArrayForClient[i] = Define.TILE_ENEMY_TANK;
                }
                if (ownBasePos != null && ownBasePos[1] + ((Define.TILEMAP_HEIGHT_MAX_VAL - ownBasePos[2]) * Define.TILEMAP_HEIGHT) == i) {
                    mapArrayForClient[i] = Define.TILE_OWN_BASE;
                }
                if (enemyBasePos != null && enemyBasePos[1] + ((Define.TILEMAP_HEIGHT_MAX_VAL - enemyBasePos[2]) * Define.TILEMAP_HEIGHT) == i) {
                    mapArrayForClient[i] = Define.TILE_ENEMY_BASE;
                }
            }

//			debugGetMap(mapArrayForClient);

            return addArray(new byte[]{Define.TILEMAP_WIDTH, Define.TILEMAP_HEIGHT}, mapArrayForClient);
        }

        return null;
    }

    public void move(int clientID, byte dir) {
        if (gameStarted) {
            Tank tank;
            if (clientID == 0)
                tank = this.tankPlayer0;
            else
                tank = this.tankPlayer1;

            if (dir == Define.DIR_UP)
                tank.moveByCell(0, 1);
            if (dir == Define.DIR_DOWN)
                tank.moveByCell(0, -1);
            if (dir == Define.DIR_LEFT)
                tank.moveByCell(-1, 0);
            if (dir == Define.DIR_RIGHT)
                tank.moveByCell(1, 0);
        }
    }

    public void shoot(int clientID, int dir) {
        if (gameStarted) {
            Tank tank;
            if (clientID == 0)
                tank = this.tankPlayer0;
            else
                tank = this.tankPlayer1;

            if (dir == Define.DIR_UP)
                tank.setRotation(0);
            if (dir == Define.DIR_DOWN)
                tank.setRotation(180);
            if (dir == Define.DIR_LEFT)
                tank.setRotation(90);
            if (dir == Define.DIR_RIGHT)
                tank.setRotation(-90);

            tank.shoot();
        }
    }

    public byte[] isGameStarted() {
        if (gameStarted)
            return new byte[]{Define.SOCK_REQ_CHECK_IS_GAME_STARTED, Define.SOCK_DATA_GAME_IS_STARTED};
        else
            return new byte[]{Define.SOCK_REQ_CHECK_IS_GAME_STARTED, Define.SOCK_DATA_GAME_IS_NOT_STARTED};
    }

    public byte[] isActionAllowed() {
        if (tankPlayer0.isActionAllowed())
            return new byte[]{Define.SOCK_REQ_CHECK_IS_ALLOWED_TO_ACT, Define.SOCK_DATA_ALLOWED_TO_ACT};
        else
            return new byte[]{Define.SOCK_REQ_CHECK_IS_ALLOWED_TO_ACT, Define.SOCK_DATA_NOT_ALLOWED_TO_ACT};
    }

    public void setMapArray(int x, int y, int tile_id) {
        mapArray[x + ((Define.TILEMAP_HEIGHT_MAX_VAL - y) * Define.TILEMAP_HEIGHT)] = (byte) tile_id;
    }


    private byte[] addArray(byte[] a, byte[] b) {
        byte[] dataRet = new byte[a.length + b.length];
        for (int i = 0; i < dataRet.length; i++) {
            if (i < a.length)
                dataRet[i] = a[i];
            else
                dataRet[i] = b[i - a.length];
        }
        return dataRet;
    }

    public void updateScore(int clientID) {
        battleScore[clientID] += 1;
        Gdx.app.log("gameplay", "score for client ID "+clientID+" = "+battleScore[clientID]);
    }

    public int getCurrentHP(int clientID) {
        if (gameStarted) {
            Tank tankObj;
            if (clientID == 0)
                tankObj = tankPlayer0;
            else
                tankObj = tankPlayer1;
            return tankObj.getActorHP();
        }
        return 0;
    }

    public Tank getTankPlayer(int clientID) {
        if (gameStarted) {
            Tank tankObj;
            if (clientID == 0)
                tankObj = tankPlayer0;
            else
                tankObj = tankPlayer1;
            return tankObj;
        }
        return null;
    }
}

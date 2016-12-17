package com.tankbattle.game.utils;

/**
 * Created by Aditya Hadi on 11/27/2016.
 */

public interface Define {
    byte SOCK_REQ_DO_INIT_CONNECTION = 0;
    byte SOCK_REQ_CHECK_IS_ALLOWED_TO_ACT = 1;
    byte SOCK_REQ_GET_CURRENT_POSITOIN = 2;
    byte SOCK_REQ_GET_MAP = 3;
    byte SOCK_REQ_DO_MOVE = 4;
    byte SOCK_REQ_DO_SHOOT = 5;
    byte SOCK_REQ_CHECK_IS_GAME_STARTED = 6;
    byte SOCK_REQ_GET_CURRENT_HPs = 7;
    byte SOCK_REQ_GET_BASE_POSITOIN = 8;

    byte SOCK_DATA_INIT_CONNECTION = 0;
    byte SOCK_DATA_LOGIN_SUCCESS = 1;
    byte SOCK_DATA_NOT_ALLOWED_TO_ACT = 0;
    byte SOCK_DATA_ALLOWED_TO_ACT = 1;
    byte SOCK_DATA_GAME_IS_NOT_STARTED = 0;
    byte SOCK_DATA_GAME_IS_STARTED = 1;

    byte DIR_UP = 0;
    byte DIR_RIGHT = 1;
    byte DIR_DOWN = 2;
    byte DIR_LEFT = 3;

    byte TILEMAP_WIDTH = 30;
    byte TILEMAP_HEIGHT = 30;
    byte TILEMAP_WIDTH_MAX_VAL = TILEMAP_WIDTH - 1;
    byte TILEMAP_HEIGHT_MAX_VAL = TILEMAP_HEIGHT - 1;

    byte TILE_EMPTY = 0;// for empty
    byte TILE_RED_WALL = 1;// for red wall
    byte TILE_SILVER_WALL = 2;// for silver wall
    byte TILE_WATER = 3;// for water
    byte TILE_OWN_TANK = 4;// for own tank
    byte TILE_ENEMY_TANK = 5;// for enemy tank
    byte TILE_OWN_BASE = 6;// for enemy tank
    byte TILE_ENEMY_BASE = 7;// for enemy tank
    byte TILE_INIT_BASE = 8;// for enemy tank
}

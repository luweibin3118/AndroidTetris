package com.lwb.tetrislib;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/1/19.
 */
public class Tetris {

    private TetrisPix[][] tetrisPixs;

    private int w, h;

    public Tetris(int w, int h) {
        this.w = w;
        this.h = h;
        tetrisPixs = new TetrisPix[4][4];
        createTetris(getDisplayPix());
    }

    public Tetris(int w, int h, List<String> display) {
        this.w = w;
        this.h = h;
        tetrisPixs = new TetrisPix[4][4];
        int m = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                String[] item = display.get(m).split(",");
                m++;
                tetrisPixs[i][j] = new TetrisPix(Integer.parseInt(item[0]), Integer.parseInt(item[1]), Boolean.parseBoolean(item[2]));
            }
        }
    }

    private void createTetris(boolean[][] displayPix) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tetrisPixs[i][j] = new TetrisPix(-3 + i, (w / 2) - 2 + j, displayPix[i][j]);
            }
        }
    }

    public TetrisPix[][] getTetrisPixs() {
        return tetrisPixs;
    }

    public void turn(boolean[][] currentSave) {
        TetrisPix[][] temp = new TetrisPix[4][4];

        boolean turnIn = true;

        if (tetrisPixs[1][1].display && tetrisPixs[1][2].display && tetrisPixs[2][1].display && tetrisPixs[2][2].display) {
            turnIn = false;
        }

        if (turnIn) {
            if (tetrisPixs[0][1].display && tetrisPixs[1][1].display && tetrisPixs[2][1].display && tetrisPixs[3][1].display) {
                turnIn = false;
            }
            if (tetrisPixs[2][0].display && tetrisPixs[2][1].display && tetrisPixs[2][2].display && tetrisPixs[2][3].display) {
                turnIn = false;
            }
        }

        if (turnIn) {
            for (int i = 0; i < 4; i++) {
                temp[0][i] = new TetrisPix(tetrisPixs[0][i].row, tetrisPixs[0][i].col, false);
                temp[i][3] = new TetrisPix(tetrisPixs[i][3].row, tetrisPixs[i][3].col, false);
            }
            for (int i = 1; i < 4; i++) {
                for (int j = 0; j < 3; j++) {
                    temp[i][j] = new TetrisPix(tetrisPixs[i][j].row, tetrisPixs[i][j].col, tetrisPixs[3 - j][i - 1].display);
                }
            }
        } else {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    temp[i][j] = new TetrisPix(tetrisPixs[i][j].row, tetrisPixs[i][j].col, false);
                }
            }
            if (tetrisPixs[1][1].display && tetrisPixs[1][2].display && tetrisPixs[2][1].display && tetrisPixs[2][2].display) {
                return;
            }

            if (tetrisPixs[0][1].display && tetrisPixs[1][1].display && tetrisPixs[2][1].display && tetrisPixs[3][1].display) {
                temp[2][0].display = temp[2][1].display = temp[2][2].display = temp[2][3].display = true;
            }
            if (tetrisPixs[2][0].display && tetrisPixs[2][1].display && tetrisPixs[2][2].display && tetrisPixs[2][3].display) {
                temp[0][1].display = temp[1][1].display = temp[2][1].display = temp[3][1].display = true;
            }

        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (temp[i][j].display) {
                    if (temp[i][j].row >= h) {
                        return;
                    }
                    if (temp[i][j].col < 0 || temp[i][j].col >= w) {
                        if (!leftOrRight(currentSave, temp, 1) && !leftOrRight(currentSave, temp, turnIn ? -1 : -2)) {
                            return;
                        }
                    }

                    if (temp[i][j].col >= 0 && temp[i][j].col < w
                            && temp[i][j].row >= 0 && temp[i][j].row < h
                            && currentSave[temp[i][j].row][temp[i][j].col]) {
                        if (!leftOrRight(currentSave, temp, 1) && !leftOrRight(currentSave, temp, turnIn ? -1 : -2)) {
                            return;
                        }
                    }
                }
            }
        }

        tetrisPixs = temp;

    }

    public boolean leftOrRight(boolean[][] currentSave, int lorSize) {
        return leftOrRight(currentSave, tetrisPixs, lorSize);
    }

    private boolean leftOrRight(boolean[][] currentSave, TetrisPix[][] tetrisPixs, int lorSize) {
        boolean moved = true;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tetrisPixs[i][j].col += lorSize;
            }
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (tetrisPixs[i][j].display) {
                    if (tetrisPixs[i][j].col >= 0 && tetrisPixs[i][j].col < w
                            && tetrisPixs[i][j].row >= 0 && tetrisPixs[i][j].row < h) {

                        if (currentSave[tetrisPixs[i][j].row][tetrisPixs[i][j].col]) {
                            moved = false;
                            break;
                        }
                    }
                    if ((tetrisPixs[i][j].col < 0 || tetrisPixs[i][j].col >= w)) {
                        moved = false;
                        break;
                    }
                }
            }
            if (!moved) {
                break;
            }
        }

        if (!moved) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    tetrisPixs[i][j].col -= lorSize;
                }
            }
        }
        return moved;
    }

    public boolean moveDown(boolean[][] currentSave) {
        boolean moved = true;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tetrisPixs[i][j].row++;
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (tetrisPixs[i][j].display) {
                    if (tetrisPixs[i][j].col >= 0 && tetrisPixs[i][j].col < w
                            && tetrisPixs[i][j].row >= 0 && tetrisPixs[i][j].row < h) {
                        if (currentSave[tetrisPixs[i][j].row][tetrisPixs[i][j].col]) {
                            moved = false;
                            break;
                        }
                    }
                    if (tetrisPixs[i][j].row >= h) {
                        moved = false;
                        break;
                    }
                }
            }
            if (!moved) {
                break;
            }
        }
        if (!moved) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    tetrisPixs[i][j].row--;
                    if (tetrisPixs[i][j].display
                            && tetrisPixs[i][j].col >= 0 && tetrisPixs[i][j].col < w
                            && tetrisPixs[i][j].row >= 0 && tetrisPixs[i][j].row < h) {
                        currentSave[tetrisPixs[i][j].row][tetrisPixs[i][j].col] = true;
                    }
                }
            }
        }
        return moved;
    }

    public boolean isGameOver() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (tetrisPixs[i][j].display && tetrisPixs[i][j].row < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Point> points = new ArrayList<>();

    public List<Point> getDisplayPoint() {
        points.clear();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (tetrisPixs[i][j].display
                        && tetrisPixs[i][j].col >= 0 && tetrisPixs[i][j].col < w
                        && tetrisPixs[i][j].row >= 0 && tetrisPixs[i][j].row < h) {
                    points.add(new Point(tetrisPixs[i][j].row, tetrisPixs[i][j].col));
                }
            }
        }
        return points;
    }

    private boolean[][] getDisplayPix() {
        boolean[][] displayPix = new boolean[4][4];
        Random random = new Random();
        int r = random.nextInt(7);
        switch (r) {
            /**
             * X
             * XXX
             */
            case 0:
                displayPix[1][0] = true;
                displayPix[2][0] = true;
                displayPix[2][1] = true;
                displayPix[2][2] = true;
                break;
            /**
             *   X
             * XXX
             */
            case 1:
                displayPix[1][2] = true;
                displayPix[2][2] = true;
                displayPix[2][1] = true;
                displayPix[2][0] = true;
                break;
            /**
             * XX
             * XX
             */
            case 2:
                displayPix[1][1] = true;
                displayPix[1][2] = true;
                displayPix[2][1] = true;
                displayPix[2][2] = true;
                break;
            /**
             * XX
             *  XX
             */
            case 3:
                displayPix[2][0] = true;
                displayPix[2][1] = true;
                displayPix[3][1] = true;
                displayPix[3][2] = true;
                break;
            /**
             *  XX
             * XX
             */
            case 4:
                displayPix[2][2] = true;
                displayPix[2][1] = true;
                displayPix[3][1] = true;
                displayPix[3][0] = true;
                break;
            /**
             *  X
             * XX
             *  X
             */
            case 5:
                displayPix[1][1] = true;
                displayPix[2][1] = true;
                displayPix[3][1] = true;
                displayPix[2][0] = true;
                break;
            /**
             *  X
             *  X
             *  X
             *  X
             */
            case 6:
                displayPix[0][1] = true;
                displayPix[1][1] = true;
                displayPix[2][1] = true;
                displayPix[3][1] = true;
                break;
            default:
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        displayPix[i][j] = true;
                    }
                }
                break;
        }
        return displayPix;
    }
}
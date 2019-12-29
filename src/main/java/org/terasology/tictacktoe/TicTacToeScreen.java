/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.tictacktoe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.utilities.Assets;

import java.util.Random;

public class TicTacToeScreen extends CoreScreenLayer {
    private UILabel yourScoreLabel;
    private UILabel cpuScoreLabel;
    private UIButton rematchButton;

    private char[][] board;
    private UIButton[][] buttons;
    private String lastPlayedBy;

    private int yourScore;
    private int cpuScore;

    private boolean isGameRunning;

    private TextureRegion cross;
    private TextureRegion crossedCross;

    private TextureRegion circle;
    private TextureRegion crossedCircle;

    private final int rows = 3;
    private final int columns = 3;

    Random random;

    @Override
    public void initialise() {
        yourScoreLabel = find("yourScore", UILabel.class);
        cpuScoreLabel = find("cpuScore", UILabel.class);
        rematchButton = find("rematchButton", UIButton.class);
        rematchButton.subscribe(button -> startGame());

        lastPlayedBy = "CPU";
        board = new char[rows][columns];
        buttons = new UIButton[rows][columns];

        cross = Assets.getTextureRegion("TicTacToe:cross").get();
        crossedCross = Assets.getTextureRegion("TicTacToe:crossedCross").get();

        circle = Assets.getTextureRegion("TicTacToe:circle").get();
        crossedCircle = Assets.getTextureRegion("TicTacToe:crossedCircle").get();

        random = new Random();

        initialiseButtons();
    }

    @Override
    public void onOpened() {
        yourScore = 0;
        cpuScore = 0;

        startGame();
    }

    private void startGame() {
        initialiseBoard();
        isGameRunning = true;

        if (lastPlayedBy.equals("PLAYER")) {
            playCPU();
        }
    }

    private void initialiseBoard() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                board[row][column] = ' ';
                buttons[row][column].setImage(null);
            }
        }

        yourScoreLabel.setText("Your Score: " + yourScore);
        cpuScoreLabel.setText("CPU Score:   " + cpuScore);
    }

    private void initialiseButtons() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                buttons[row][column] = find("button" + (row * columns + column + 1), UIButton.class);
                subscribeButton(buttons[row][column]);
            }
        }
    }

    private void subscribeButton(UIButton uiButton) {
        uiButton.subscribe((button) -> {
            if (!isGameRunning)
                return;
            lastPlayedBy = "PLAYER";
            String id = button.getId();
            int index = Integer.parseInt(id.substring(id.length() - 1)) - 1;
            int row = index / columns;
            int column = index % 3;
            if(board[row][column] != ' ')
                return;
            board[row][column] = 'X';
            buttons[row][column].setImage(cross);
            checkGame();
            playCPU();
        });
    }

    private void playCPU() {
        if(!isGameRunning)
            return;
        int chosenRow = random.nextInt(rows);
        int chosenColumn = random.nextInt(columns);
        if (board[chosenRow][chosenColumn] == ' ') {
            lastPlayedBy = "CPU";
            board[chosenRow][chosenColumn] = 'O';
            buttons[chosenRow][chosenColumn].setImage(circle);
            checkGame();
        } else {
            playCPU();
        }
    }

    private void checkGame() {
        if (winnerExists()) {
            declareWinner();
            if (lastPlayedBy == "PLAYER") {
                yourScore++;
                yourScoreLabel.setText("Your Score: " + yourScore);
            } else {
                cpuScore++;
                cpuScoreLabel.setText("CPU Score:   " + cpuScore);
            }
            isGameRunning = false;
        }
        if (!hasFreeSpace())
            isGameRunning = false;
    }

    private boolean hasFreeSpace() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (board[row][column] == ' ') {
                    return true;
                }
            }
        }
        return false;
    }

    private int getRowCrossed() {
        for (int row = 0; row < rows; row++) {
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2] && board[row][0] != ' ')
                return row;
        }
        return -1;
    }

    private int getColumnCrossed() {
        for (int column = 0; column < columns; column++) {
            if (board[0][column] == board[1][column] && board[1][column] == board[2][column] && board[0][column] != ' ')
                return column;
        }
        return -1;
    }

    private int getDiagonalCrossed() {
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != ' ')
            return 0;

        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != ' ')
            return 1;

        return -1;
    }

    private boolean winnerExists() {
        return (getRowCrossed() != -1 || getColumnCrossed() != -1
                || getDiagonalCrossed() != -1);
    }

    private void declareWinner() {
        TextureRegion texture = null;

        if (lastPlayedBy.equals("PLAYER")) {
            texture = crossedCross;
        } else if(lastPlayedBy.equals("CPU")) {
            texture = crossedCircle;
        }

        int crossedRow = getRowCrossed();
        int crossedColumn = getColumnCrossed();
        int crossedDiagonal = getDiagonalCrossed();

        if (crossedRow != -1) {
            changeRowTexture(crossedRow, texture);
        }
        if (crossedColumn != -1) {
            changeColumnTexture(crossedColumn, texture);
        }
        if (crossedDiagonal != -1) {
            changeDiagonalTexture(crossedDiagonal, texture);
        }
    }

    private void changeRowTexture(int row, TextureRegion texture) {
        for (int column = 0; column < columns; column++) {
            buttons[row][column].setImage(texture);
        }
    }

    private void changeColumnTexture(int column, TextureRegion texture) {
        for (int row = 0; row < rows; row++) {
            buttons[row][column].setImage(texture);
        }
    }

    private void changeDiagonalTexture(int diagonal, TextureRegion texture) {
        if (diagonal == 0) {
            buttons[0][0].setImage(texture);
            buttons[1][1].setImage(texture);
            buttons[2][2].setImage(texture);
        }
        if (diagonal == 1) {
            buttons[0][2].setImage(texture);
            buttons[1][1].setImage(texture);
            buttons[2][0].setImage(texture);
        }
    }
}

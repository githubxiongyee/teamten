// Copyright 2011 Lawrence Kesteloot

package com.teamten.mario;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Side-scroller that uses AI to get to its goal.
 */
public class Mario extends JFrame {
    private static final int ANIMATION_MS = 40;
    private World mWorld;
    private WorldDrawer mWorldDrawer;
    private volatile Input mInput;
    private boolean mRunning = true;
    private final Searcher mSearcher;

    public static void main(String[] args) {
        new Mario();
    }

    private Mario() {
        Env env = Env.makeEnv();
        Player player = new Player((Env.WIDTH - Player.WIDTH)/2, Env.HEIGHT/2);
        mWorld = new World(env, player);
        mWorldDrawer = new WorldDrawer(mWorld);
        mInput = Input.NOTHING;
        mSearcher = new Searcher();

        makeUi(mWorldDrawer);
        hookUpInput(mWorldDrawer);
        startAnimationTimer();
    }

    private void makeUi(WorldDrawer worldDrawer) {
        JPanel content = (JPanel) getContentPane();
        content.setLayout(new BorderLayout());
        content.add(worldDrawer, BorderLayout.CENTER);

        setSize(Env.WIDTH*4, Env.HEIGHT*4);
        setVisible(true);
    }

    private void startAnimationTimer() {
        Timer timer = new Timer(ANIMATION_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (mRunning) {
                    mWorld = mWorld.step(mInput);
                    mWorldDrawer.setWorld(mWorld);
                }
            }
        });
        timer.start();
    }

    private void takeAutomatedStep() {
        Point point = getMousePosition();
        if (point != null) {
            mWorldDrawer.reverseTransform(point);
            System.out.printf("%d %d%n", point.x, point.y);

            Input input = mSearcher.findBestMove(mWorld, point);

            mWorld = mWorld.step(input);
            mWorldDrawer.setWorld(mWorld);
        }
    }

    private void hookUpInput(WorldDrawer worldDrawer) {
        worldDrawer.addKeyListener(new KeyListener() {
            @Override // KeyListener
            public void keyPressed(KeyEvent keyEvent) {
                keyActuated(keyEvent, true);
            }

            @Override // KeyListener
            public void keyReleased(KeyEvent keyEvent) {
                keyActuated(keyEvent, false);
            }

            @Override // KeyListener
            public void keyTyped(KeyEvent keyEvent) {
                switch (keyEvent.getKeyChar()) {
                    case 'a':
                        mRunning = false;
                        takeAutomatedStep();
                        break;

                    case 'r':
                        mRunning = !mRunning;
                        break;
                }
            }

            private void keyActuated(KeyEvent keyEvent, boolean pressed) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        mInput = mInput.withJumpPressed(pressed);
                        break;

                    case KeyEvent.VK_LEFT:
                        mInput = mInput.withLeftPressed(pressed);
                        break;

                    case KeyEvent.VK_RIGHT:
                        mInput = mInput.withRightPressed(pressed);
                        break;
                }
            }
        });
    }
}

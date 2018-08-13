package ro.luca1152.typing.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import ro.luca1152.typing.TypingGame;
import ro.luca1152.typing.actors.Crate;
import ro.luca1152.typing.actors.GameMap;

public class PlayScreen extends ScreenAdapter {
    private final TypingGame game;
    private Stage stage;
    private static GameMap map;
    private Crate targetCrate = null;
    private float delay = 0f;

    public PlayScreen(TypingGame game) {
        this.game = game;
        stage = new Stage(game.getViewport(), game.getBatch());
    }

    @Override
    public void show() {
        map = new GameMap(game, 0);
        stage.addActor(map);
        setInputProcessor();
    }

    private void setInputProcessor() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (PlayScreen.map.getTurret().isRemovedTargetCrate())
                    targetCrate = null;
                if (keycode >= Keys.A && keycode <= Keys.Z && delay <= 0f) {
                    if (targetCrate == null) {
                        for (Actor child : PlayScreen.map.getCrates().getChildren()) {
                            Crate crate = ((Crate) child);
                            if (crate.firstCharFromWord() == keycodeToChar(keycode)) {
                                targetCrate = crate;
                                crate.keyPressed(keycodeToString(keycode));
                                if (targetCrate.wordIsEmpty() || targetCrate.reachedFinish()) {
                                    targetCrate = null;
                                }
                                PlayScreen.map.getTurret().shoot(targetCrate, true);
                                return true;
                            }
                        }
                    } else {
                        // The word is in progress
                        if (targetCrate.firstCharFromWord() == keycodeToChar(keycode)) {
                            targetCrate.keyPressed(keycodeToString(keycode));
                            PlayScreen.map.getTurret().shoot(targetCrate, false);
                        }

                        // While typing, the box reached the finish point.
                        // May result in unexpected key presses, hence the delay.
                        else if (!targetCrate.wordIsEmpty() && targetCrate.reachedFinish()) {
                            delay = .2f;
                            targetCrate = null;
                            PlayScreen.map.getTurret().removeTargetCrate();
                        }
                    }
                }
                return false;
            }
        });
    }

    private char keycodeToChar(int keycode) throws IllegalArgumentException {
        if (keycode < 29 || keycode > 54)
            throw new IllegalArgumentException("Keycode out of range.");
        return (char) (keycode + 68);
    }

    private String keycodeToString(int keycode) throws IllegalArgumentException {
        return Character.toString(keycodeToChar(keycode));
    }

    @Override
    public void render(float delta) {
        delay -= delta;
        stage.act(delta);
        Gdx.gl20.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}

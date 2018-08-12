package ro.luca1152.typing.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;

import ro.luca1152.typing.TypingGame;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.after;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;

public class GameMap extends Group {
    private final TypingGame game;

    // Map
    private ArrayList<String> allWords = new ArrayList<String>();
    private JsonValue currentMap;
    private Vector2[] finishPoints;
    private int id;

    // Children
    private Image mapImage;
    private Turret turret;
    private Group crates;

    private float crateTimer = 0f;

    public GameMap(TypingGame game, int mapId) {
        this.game = game;
        this.id = mapId;

        mapImage = new Image(game.getManager().get("maps/map" + mapId + ".png", Texture.class));
        addActor(mapImage);

        turret = new Turret(game, 284f, 284f);
        addActor(turret);

        crates = new Group();
        addActor(crates);

        JsonReader jsonReader = new JsonReader();
        currentMap = jsonReader.parse(Gdx.files.internal("maps/maps.json")).get(id);
        finishPoints = finishPoints(currentMap);
    }

    private Vector2[] finishPoints(JsonValue currentMap) {
        int numOfFinishPoints = currentMap.getInt("numOfFinishPoints");
        Vector2[] finishPoints = new Vector2[numOfFinishPoints];
        for (int i = 0; i < numOfFinishPoints; i++) {
            JsonValue arrayOfPoints = currentMap.get("finishPoints");
            if (numOfFinishPoints == 1)
                finishPoints[i] = new Vector2(arrayOfPoints.getInt("x"), arrayOfPoints.getInt("y"));
            else
                finishPoints[i] = new Vector2(arrayOfPoints.get(i).getInt("x"), arrayOfPoints.get(i).getInt("y"));
        }
        return finishPoints;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        spawnCrates(delta);
        removeCratesAtFinishPoint();
    }

    private void spawnCrates(float delta) {
        crateTimer -= delta;
        if (crateTimer <= 0f) {
            crateTimer = 1.5f;

            int randomSpawn = MathUtils.random(0, currentMap.getInt("numOfSpawns") - 1);
            Crate crate = newCrate(currentMap, randomSpawn);
            crates.addActor(crate);
            addMovementActionsTo(crate, randomSpawn, currentMap);
        }
    }

    private void removeCratesAtFinishPoint() {
        for (Actor crate : crates.getChildren()) {
            for (Vector2 finishPoint : finishPoints) {
                if (crate.getX() == finishPoint.x * 64 && crate.getY() == finishPoint.y * 64) {
                    ((Crate) crate).removeCrate();
                }
            }
        }
    }

    private Crate newCrate(JsonValue currentMap, int randomSpawn) {
        JsonValue spawn = currentMap.get("spawns").get(randomSpawn);
        return new Crate(game, allWords, spawn.getInt("x"), spawn.getInt("y"));
    }

    private void addMovementActionsTo(Crate crate, int randomSpawn, JsonValue currentMap) {
        JsonValue path = currentMap.get("spawns").get(randomSpawn).get("path");
        for (JsonValue value : path)
            crate.addAction(after(moveTo(value.getInt("x") * 64, value.getInt("y") * 64, value.getInt("distance") / 2f)));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        mapImage.draw(batch, parentAlpha);
        turret.draw(batch, parentAlpha);
        for (Actor child : crates.getChildren())
            ((Crate) child).getCrateImage().draw(batch, parentAlpha);
        for (Actor child : crates.getChildren())
            ((Crate) child).getLabel().draw(batch, parentAlpha);
    }

    public Turret getTurret() {
        return turret;
    }

    public Group getCrates() {
        return crates;
    }
}

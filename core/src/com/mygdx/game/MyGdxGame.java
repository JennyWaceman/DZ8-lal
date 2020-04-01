package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class MyGdxGame extends ApplicationAdapter {
    private Texture dropImage;
    private Texture bucketImage;
    private Texture appleImage;
    private Sound dropSound;
    private Music rainMusic;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Rectangle bucket;
    private Array<Rectangle> rainDrops;
    private long lastDropItem;
    private Array<Rectangle> Apples;
    private long lastDropApple;
    private boolean drawBucket = true;


    private void spawnRainDrop() {
        Rectangle rainDrop = new Rectangle();
        rainDrop.x = MathUtils.random(0, 800 - 64);
        rainDrop.y = 480;
        rainDrop.width = 64;
        rainDrop.height = 64;
        rainDrops.add(rainDrop);
        lastDropItem = TimeUtils.nanoTime();
    }

    private void spawnApples() {
        Rectangle Apple = new Rectangle();
        Apple.x = MathUtils.random(0, 800 - 64);
        Apple.y = 480;
        Apple.width = 64;
        Apple.height = 64;
        Apples.add(Apple);
        lastDropApple = TimeUtils.millis();
    }

    @Override
    public void create() {
        super.create();

        dropImage = new Texture(Gdx.files.internal("droplet.png"));
        bucketImage = new Texture(Gdx.files.internal("bucket.png"));
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        appleImage = new Texture(Gdx.files.internal("Apple.png"));

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        batch = new SpriteBatch();

        bucket = new Rectangle();
        bucket.x = 800 / 2 - 64 / 2;
        bucket.y = 20;
        bucket.width = 64;
        bucket.height = 64;

        rainDrops = new Array<Rectangle>();
        spawnRainDrop();

        Apples = new Array<Rectangle>();
        spawnApples();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // обновление камеры
        batch.setProjectionMatrix(camera.combined);

        // указываем SpriteBatch координаты системы для камеры
        batch.begin();

        // отрисовка ведра, только в том случае, если яблоко в ведро не попало
        if(drawBucket) {
            batch.draw(bucketImage, bucket.x, bucket.y);
        }

        for (Rectangle raindrop : rainDrops) {
            batch.draw(dropImage, raindrop.x, raindrop.y);
        }

        for (Rectangle appledrop : Apples) {
            batch.draw(appleImage, appledrop.x, appledrop.y);
        }


        batch.end();



        // передвижение корзины по экрану
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos); // чтобы клик по экрану
            // расчитывался в пределах viewport'a (ширины и высоты экрана)
            bucket.x = touchPos.x - 64 / 2;
        }

        // перемещение на стрелки клавиатуры
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucket.x += 200 * Gdx.graphics.getDeltaTime();
        }

        // делаем, чтобы ведро не уходило за пределы экрана
        if (bucket.x < 0) {
            bucket.x = 0;
        }
        if (bucket.x > 800 - 64) {
            bucket.x = 800 - 64;
        }

        // проверяем сколько времени прошло после последней капельки, если больше 1000...,
        // то создаем новую
        if (TimeUtils.nanoTime() - lastDropItem > 1000000000) {
            spawnRainDrop();
        }

        if (TimeUtils.millis() - lastDropApple > 4000) {
            spawnApples();
        }

        // падение капель, удаление капель, воспроизведение звука при падении в ведро
        for (Iterator<Rectangle> iter = rainDrops.iterator(); iter.hasNext(); ) {
            Rectangle raindrop_loop = iter.next();
            raindrop_loop.y -= 200 * Gdx.graphics.getDeltaTime();

            // как только капля попадает за нижнюю границу, она удаляется
            if (raindrop_loop.y + 64 < 0) {
                iter.remove();
            }

            // если капля пересекат ведро, то выполняется тело условия
            if (raindrop_loop.overlaps(bucket)) {
                dropSound.play();
                iter.remove();
            }

        }
        for (Iterator<Rectangle> itera = Apples.iterator(); itera.hasNext(); ) {
            Rectangle apple_loop = itera.next();
            apple_loop.y -= 200 * Gdx.graphics.getDeltaTime();

            if (apple_loop.y + 64 < 0) {
                itera.remove();
            }

            if (apple_loop.overlaps(bucket)) {
                itera.remove();
                drawBucket = false;
                bucketImage.dispose();
            }
        }
        camera.update();
    }


    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        appleImage.dispose();
        batch.dispose();

    }
}

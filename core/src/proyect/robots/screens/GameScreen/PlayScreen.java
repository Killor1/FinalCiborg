package proyect.robots.screens.GameScreen;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.*;

import proyect.robots.MyGame;
import proyect.robots.entities.Drop;
import proyect.robots.entities.Png;
import proyect.robots.entities.player.Player;
import proyect.robots.screens.MyScreen;
import proyect.robots.screens.Scores.InsertScoreScreen;
import proyect.robots.utils.Crect;
import proyect.robots.utils.ParticleEffectActor;
import proyect.robots.utils.WorldPhysychs;
import proyect.robots.utils.loaders.MapGenerator;
import proyect.robots.utils.loaders.SoundAssets;

public class PlayScreen extends MyScreen {
	
	final int cellW=16;
	final int cellH=16;
		
	private World world;
    private Box2DDebugRenderer b2dr;

	public SpriteBatch enemieBatch;
	private OrthographicCamera camera;

    public TiledMap map;
    private OrthogonalTiledMapRenderer renderer;     
    public Player player;

    public float elapsedTime;
    ShapeRenderer shaperenderer;
    
    public Array<Crect> ground;
    public Array<Crect> ceiling ;
    public Array<Crect> right ;
    public Array<Crect> left ;
    public Array<Crect> death ;
    public Array<Crect> block;
    public Array<Crect> respawn;
    
    public ArrayList<Array<Crect>> mapBounds;
    
   
    public ShapeRenderer shapeRenderer;
    public ShapeRenderer srEnemies;
    
    ParticleEffect pe;
    public SpriteBatch partiBatch;
    ParticleEffectActor pea ;
    public SpriteBatch batch;
    public String map2;
    
    public ArrayList<Png> enemies1;
    public MapGenerator enemiesLoader;    
    
    public HUD hud;
    public Viewport viewport;
    
    public ArrayList<Drop> crafting;
    
    public MyRenderer rend;
    
    public String currStage;
    public String playerColor;
    
    public Stage stage;
    public PauseMenu pauseMenu;
    public DeathScreen deathScreen;
    public InsertScoreScreen insScreen;
    public static boolean onpause;
    public int score;
    public static ArrayList<Png> remEnem;
    
    public WorldPhysychs fisicas;
    public Matrix4 debugMatrix;
    public float px, py;
    public String mapName;
    
    public boolean beated;
    public Vector2 respPos;
    public Music song;
    
    public PlayScreen(Game game, String map, String playerColor, float x, float y) {
    	super(game);
    	mapName=map;
    	Vector2 gravity = new Vector2(0, -9.8f);
    
    	world = new World(gravity, true);
    	score=0;
    	this.playerColor = playerColor;
    	this.map=new TmxMapLoader().load("maps/"+map+".tmx");
    	currStage=map;
    	ground = new Array<Crect>();
        ceiling = new Array<Crect>();
        right = new Array<Crect>();
        left = new Array<Crect>();
        death = new Array<Crect>();
        block = new Array<Crect>();
        respawn = new Array<Crect>();
        mapBounds = new ArrayList<Array<Crect>>();
        enemies1 = new ArrayList<Png>(); 
        px=x;
        py=y;    
        hud = new HUD(world, this);
        beated=false;
    }
    
    public void setSong (Music m){
    	this.song=m;
    	
    }
    @Override
	public void show() { 
        shapeRenderer = new ShapeRenderer();
        srEnemies = new ShapeRenderer();
    	batch = new SpriteBatch();
    	
    	camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.zoom=1/4.5f;
		shaperenderer = new ShapeRenderer();
    	
		viewport =  new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        viewport.apply();
        
        renderer = new OrthogonalTiledMapRenderer(map);
		renderer.setView(camera);		
		
		b2dr = new Box2DDebugRenderer();
		// 2684, 230);
		player= new Player(this,px, py, playerColor);
		respPos = new Vector2(px, py);
		
		fisicas = new WorldPhysychs(this, world);
		fisicas.shape.setAsBox(player.width/2/100, player.height/2/100);
		fisicas.fdef.shape= fisicas.shape;
		player.body.createFixture(fisicas.shape,1);
		player.body.setUserData(player);

		enemieBatch = new SpriteBatch();
		partiBatch= new SpriteBatch();	
		
		crafting = new ArrayList<Drop>();	
		
		rend = new MyRenderer(this);
		
		stage =  new Stage();
		pauseMenu = new PauseMenu(game, batch);
		insScreen = new InsertScoreScreen(game, this);
		deathScreen = new DeathScreen(game, batch, this);

		remEnem = new ArrayList<Png>();
		try{
			if(SoundAssets.introSong.isPlaying()){
				SoundAssets.introSong.stop();
			}
			song.setLooping(true);
			song.setVolume(MyGame.musicValue);
			song.play();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			
		}
		
		//insScreen = new InsertScoreScreen(game, this, batch);
		
		
	}   
  
   

    public void update(float dt){
    	if(Gdx.input.isKeyPressed(Input.Keys.P)){
			onpause=true;
		}    	
    	//System.out.println("entra update playS");
       world.step(Gdx.graphics.getDeltaTime(), 6, 2);
       if(!onpause && !player.isDied() && !beated){
    	   player.Update(dt);
    	   Player.bullets.removeAll(remEnem);
    	   Player.remBull.clear();
           for (Iterator<Png> it =enemies1.iterator();it.hasNext();){
        	   Png p=it.next();
        	   p.Update(dt);
           }  
           enemies1.removeAll(remEnem);
           remEnem.clear();
       }
       //attach our gamecam to our players.x coordinate
       camera.position.set(player.body.getPosition().x*100,player.body.getPosition().y*100,0);
       camera.position.x = MathUtils.clamp(camera.position.x+player.getRegionWidth()+8,0+player.getRegionWidth()+100 , 25000);
       camera.update();
       
       renderer.setView(camera);        
    }
    
	@Override
	public void render(float delta) {
		world.step(Gdx.graphics.getDeltaTime(), 8, 3);
		update(delta);		
		elapsedTime += Gdx.graphics.getDeltaTime();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		renderer.render();		
		
		batch.setProjectionMatrix(camera.combined);
		enemieBatch.setProjectionMatrix(camera.combined);		
		
		rend.renderItems(delta, batch);
		
		if(currStage.equals("stage1")){
			rend.renderLvlOneEnemies(delta, elapsedTime, batch);
		}else if (currStage.equals("stage2")){
			rend.renderLvl2Enemies(delta, elapsedTime, batch);
		}
		rend.renderPlayer(delta, elapsedTime, batch);
	
		hud.render();		
		
		if(onpause){
			pauseMenu.render(delta);
		}else if (Player.deathScreen){			
			deathScreen.render(delta);
		}else if(beated){
			song.stop();
			insScreen.render(delta);
			SoundAssets.introSong.setLooping(true);
    		SoundAssets.introSong.play();
    		SoundAssets.introSong.setVolume(MyGame.musicValue*0.5f);
		}
		
		//debugRender();	
		
		
	}

	public void debugRender() {
		batch.begin();
		b2dr.render(world, camera.combined.cpy().scale(100, 100, 1));
		batch.end();
		drawBounds();
	}	
	
	private void drawBounds() {
		//Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.position.set(player.getX(), player.getY(), 0);
		camera.update();
		shapeRenderer.setProjectionMatrix(camera.combined);		
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);	

	
		for (Array<Crect> ar:mapBounds){
			for (Crect r:ar){				
				shapeRenderer.rect(r.x, r.y, r.width, r.height);
			}
		}
		shapeRenderer.rect(player.bounds.x, player.bounds.y, player.bounds.width, player.bounds.height);		
		shapeRenderer.end();
		
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.setColor(Color.YELLOW);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.rect(player.bounds.x, player.bounds.y, player.bounds.width, player.bounds.height);
		for(Png p: enemies1){	
			
			shapeRenderer.rect(p.bounds.x, p.bounds.y, p.bounds.width, p.bounds.height);
		}
		shapeRenderer.end();
		/*for(Iterator<FocusedShoot> itr = worm.shoots.iterator(); itr.hasNext(); ){
			FocusedShoot b = itr.next();	
			shapeRenderer.rect(b.bounds.x, b.bounds.y, b.bounds.width, b.bounds.height);
		}*/
		
		/*
		srEnemies.setProjectionMatrix(camera.combined);
		srEnemies.setColor(Color.RED);

		srEnemies.begin(ShapeRenderer.ShapeType.Line);
		for (Array<Crect> ar:mapBounds){
			for (Crect r:ar){				
				srEnemies.rect(r.x, r.y, r.width, r.height);
			}
		}*/
		//srEnemies.rect(robot.bounds.x, robot.bounds.y, robot.bounds.width, robot.bounds.height);
		//srEnemies.rect(worm.bounds.x, worm.bounds.y, worm.bounds.width, worm.bounds.height);
		//srEnemies.rect(worm.shootBounds.x, worm.shootBounds.y, worm.shootBounds.width, worm.shootBounds.height);
		srEnemies.end();
	}

	@Override
	public void resize(int width, int height) {
		hud.resize(width,height);
		viewport.update(width, height);
    	viewport.apply();
	}

	@Override
	public void hide() {
		renderer.dispose();
		enemieBatch.dispose();
		srEnemies.dispose();
		shapeRenderer.dispose();
		map.dispose();
		batch.dispose();		
		b2dr.dispose();
	}

	public World getWorld(){
		return this.world;
	}
	public TiledMap getMap(){
		return map;
	}
	public Player getPlayer(){
		return player;
	}
	
}
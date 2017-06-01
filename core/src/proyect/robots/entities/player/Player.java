package proyect.robots.entities.player;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import proyect.robots.MyGame;
import proyect.robots.entities.Drop;
import proyect.robots.entities.Png;
import proyect.robots.screens.PlayScreen;
import proyect.robots.utils.Crect;
import proyect.robots.utils.CustomAnim;
import proyect.robots.utils.loaders.LoadResources;
import proyect.robots.utils.loaders.SoundAssets;

public class Player extends Png{	
	public int state, action, position, dir;
	//int state
	public static final int STAND = 0;
    public static final int RUN = 1;
    // int action
    public static final int JUMP = 0;
    static final int SHOOT = 1;
    public static final int NOTHING = 2;
    public static final int FALLING = 3;
    // int position
    public static final int GROUND = 0;
    public static final int HOLDU = 1;
    public static final int HOLDL = 2;
    public static final int HOLDR = 3;
    public static final int DEATH = 4;
    public static final int BLOCK = 5;
    public static final int NONE = 6;
    //int dir
    public static final int LEFT = -1;
    public  static final int RIGHT = 1;   
    //movement variables:
    public static  float JUMP_VELOCITY = 9.8f;
    public static float GRAVITY = -9.8f;
    public static  float SPEED = 0.5f;
    public static float CLIMBSPEED = 1;    
    public final Vector2 caer= new Vector2(0,GRAVITY);
    //Movement control :
    public static final float MAX_JUMP_SPEED   = 4f;
	public Vector2 pos = new Vector2();
    public Vector2 speed = new Vector2(0, 0);
    public PlayerMovementController moveController;
	//Animations control:
	public ArrayList<CustomAnim> anims = new ArrayList<CustomAnim>();
	public CustomAnim curanim;	
	public CustomAnim shield, dust;  
    //shooting control:
	public static final long FIRE_RATE = 300000000L;
    public enum ShootingDir {R,L,U,D,RU,RD,LD,LU}
	public ShootingDir sdir;
	public boolean shooting;
	public float shootDelay;
	public static ArrayList<Shoot> bullets,remBull;
	//Audio elements:
	public Music footSteps, misile1;
    public Sound playerShoot, misile2;    
    public Long shootId;
	//Stats control: 
    public static float hp;
    public static int rocketCounter, life;
    public static boolean shielded, rocket, died;
    public float shieldTime, points, stateTime;
    
    public Body body;
    public World world;	
	
	public long lastShot;    
    
    public Rectangle collx, colly;
	@SuppressWarnings("unused")
	private boolean contacting; 
	public static boolean deathScreen;
	public float width = 8, height= 16;
	
    public Player(PlayScreen screen, float x, float y, String color) {
		super(screen, x, y);
		width= 8;
		height=16;
		//init the player elements:
		bullets= new ArrayList<Shoot>();
		remBull = new ArrayList<Shoot>();
		collx = new Rectangle();
		colly = new Rectangle();
		bounds = new Rectangle(pos.x-2, pos.y-2, 12, 20);		
		moveController = new PlayerMovementController(this);
		//load animations and effects: 
		anims=LoadResources.loadPlayerAnimations(color);
		curanim=CustomAnim.getById(anims,"stand");
		loadPlayerEffects();
		footSteps = SoundAssets.footSteps;
		footSteps.setVolume(0.5f);
		playerShoot = SoundAssets.playerShoot;
		misile1= SoundAssets.misile1;
		misile2 = SoundAssets.misile2;	
		world= screen.getWorld();
		
		initializePlayer();		
	}
    
    public void initializePlayer(){
    	BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(getX()/100, getY()/100);
		body= world.createBody(bodyDef);
		body.setFixedRotation(true);	
		points=0;
	    stateTime = 0;
	    shootDelay=0;
	    sdir=ShootingDir.L;		
		state=STAND;
		action=FALLING;
		position=GROUND;
		dir=LEFT;	
		rocketCounter=0;		
		hp=1;
		life = 20;
		died = false;
    }
	
	public void getAction(float delta){
		switch(position){
			case 0: //ground
				moveController.moveOnGround(delta);
				break;
			case 1://hold up
				moveController.moveOnCeiling(delta);
				break;
			case 2://hold left
				dir=LEFT;
				moveController.moveOnLeftSide(delta);
			break;
			case 3://hold right
				moveController.moveOnRightSide(delta);
				break;			
			default:	
				break;
		}
		 if(Gdx.input.isKeyPressed(Input.Keys.P)){
				PlayScreen.onpause=true;
			}
	}
	
	public void addShoot() {
		if(rocket){
			if(System.nanoTime() - lastShot >= FIRE_RATE) {
				rocketCounter--;
				action=SHOOT;
				GuidedShoot s= new GuidedShoot(this, "misile.png", "fire.p");				
				bullets.add(s);
				misile1.play();
				misile1.setVolume(1*MyGame.fxValue);
				if(!misile1.isPlaying()){
					misile2.play(1*MyGame.fxValue);
				}
				lastShot = System.nanoTime();

			}			
			if(rocketCounter<=0){
				rocket=false;
			}
		}else{
			if(System.nanoTime() - lastShot >= FIRE_RATE) {
				action=SHOOT;
				Shoot s= new Shoot(this, "greenBullet.png", "effect.p");				
				bullets.add(s);
				shootId = playerShoot.play(1*MyGame.fxValue);
				lastShot = System.nanoTime();
			}
		}		
	}	
	
	public void jump(float delta){		
		curanim=CustomAnim.getById(anims, "jumpR");
		body.setLinearVelocity(body.getLinearVelocity().x, body.getLinearVelocity().y);
		curanim=CustomAnim.getById(anims, "jumpR");

		if(body.getLinearVelocity().y<0){
			action = FALLING;
			curanim = CustomAnim.getById(anims, "stand");
		}
	}	
	int cont=0;
	@Override
	public void Update (float delta){
		
		// TODO
		//provisional:
		//armRockets();
		//shieldPlayer();
		if(!died){
			hp=(float)life/100;
			
			if(shielded){
				shieldTime+=delta;
				if(shieldTime>=10){
					shielded=false;
				}
			}
		
			if(action != JUMP){
				getAction(delta);
				if(state == RUN){
					footSteps.play();
					footSteps.setVolume(0.8f*MyGame.fxValue);				
				}
			}else{
				jump(delta);
			}		
			
			checkPlayerCollisions(delta);
		
			pos.x=body.getPosition().x*100;
			pos.y=body.getPosition().y*100;
			setX(pos.x);
			setY(pos.y);
			bounds.setX(pos.x-2-(width/2));
			bounds.setY(pos.y-2-(height/2));
			
		}else{
			curanim = CustomAnim.getById(anims, "diyingR");
			cont++;
			if(cont>=curanim.getKeyFrames().length){				
				deathScreen=true;
			}
		}		
	}

    public void checkPlayerCollisions(float delta){
    	contacting=false;
    	for (Array<Crect> rs: getScreen().mapBounds){
    		for (Crect r:rs ){
    			if(r.overlaps(bounds)){    				
    				String s= r.getId();
    				if(s.equals("Ground")){
    					collx=r; 
    	    			position=GROUND; 
    	    			contacting=true;
    				}else if (s.equals("Ceiling")){
    					collx=r;
    	    			position=HOLDU; 
    	    			contacting=true;
    				}else if (s.equals("Left")){
    					colly=r; 
    	    			position=HOLDL; 
    	    			contacting=true;
    				}else if (s.equals("Right")){
    					colly=r;
    	    			position=HOLDR; 
    	    			contacting=true;
    				}else if (s.equals("Death")){
    					System.out.println("toca en ddeath");
    					// TODO
    					collx=r; 
    					died = true;
    	    			contacting=true;
    				}else if (s.equals("Block")){
    	    			colly=r;
    	    			contacting=true;
    				}
    			}    			
    		}
    		if(colly !=null && collx !=null){
    			if(collx.overlaps(colly) && (bounds.overlaps(collx) || (bounds.overlaps(colly)))){
        			if(position == HOLDR){
        				if ((body.getPosition().y*100>=colly.y+colly.height)){
        					body.setTransform((collx.x+(width/2))/100, (collx.y+collx.height+height)/100,0);
        					position=GROUND;
        					colly=null;        					
        				}else if((body.getPosition().y*100<=colly.y)){
        					position=NONE;
        					body.setLinearVelocity(caer);
        					colly=null;
        				}
        			}else if(position == HOLDL){
        				if ((body.getPosition().y*100>=colly.y+colly.height)){
        					body.setTransform((collx.x+collx.width-(width/2))/100, (collx.y+collx.height+height)/100,0);
        					position=GROUND;
        					colly=null;        		
        				}else if((body.getPosition().y*100<=colly.y)){
        					position=NONE;
        					body.setLinearVelocity(caer);
        					colly=null;        		
        				}
        			}
        		}
    		}    		
    	}    	 	
    	
    	for (Iterator<Drop> itr = getScreen().crafting.iterator();itr.hasNext();){
    		Drop d=itr.next();
    		if (d.bounds.overlaps(bounds)){
    			if (d.getDropType().equals("shield")){
    				shieldPlayer();
    			}else if(d.getDropType().equals("rocket")){
    				armRockets();
    			}else if(d.getDropType().equals("life")){
    				addLife();
    			}
    			itr.remove();
    		}
    	}  
    	
    	if(position == HOLDU){			
			if ( (body.getPosition().x*100>collx.x+collx.width) || (body.getPosition().x*100<collx.x) ){
				position=NONE;
				body.setLinearVelocity(caer);	
			}
			 if (Gdx.input.isKeyJustPressed(Input.Keys.ALT_RIGHT)) {
				 position=NONE;
				 body.setLinearVelocity(caer);	
			 }
		}	
    }	
    
	public void loadPlayerEffects(){
		Array<TextureRegion> frames = new Array<TextureRegion>();
		TextureAtlas atlas= new TextureAtlas("Personaje/efectosPlayer.txt");
		Array<AtlasRegion> regs=atlas.findRegions("b");
		for (AtlasRegion r:regs){			
    		frames.add(new TextureRegion(r));    		    		
    	}		
		shield=new CustomAnim(0.29f,frames);
		frames.clear();		
		for (int i=0;i<=9;i++){
			StringBuilder sb2 = new StringBuilder("0");
    		frames.add(new TextureRegion(atlas.findRegion(sb2.append(i).toString())));    		    		
    	}		
		for (int i=0;i<=4;i++){
			StringBuilder sb3= new StringBuilder("1");
    		frames.add(new TextureRegion(atlas.findRegion(sb3.append(i).toString())));    		    		
    	}
		dust= new CustomAnim(1f,frames);
		frames.clear();		
	}
	
	public void armRockets(){
		rocketCounter=50;
		rocket = true;		
	}
	
	public void shieldPlayer(){
		shielded=true;
		shieldTime=0;
	}
	
	public void addLife(){
		life+=20;
		if(life>=100){
			life=100;
		}
		hp=life/100;
	}
	public static void hurtPlayer(int dmg){
		life-=dmg;
		if(life<=0){
			died=true;
		}
	}	
}
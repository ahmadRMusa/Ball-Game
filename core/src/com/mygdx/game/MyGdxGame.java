package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Array;


import sun.security.acl.WorldGroupImpl;


public class MyGdxGame extends ApplicationAdapter {
//public class MyGdxGame extends BaseBulletTest {
	
	private PerspectiveCamera cam;
	public ModelBatch modelBatch;
	public Model model;
	public ModelInstance instance;
	public Environment environment;
	public CameraInputController camController;
	
	public AssetManager assets; // Loads and stores assets like textures, bitmapfonts, tile maps, sounds, music and so on.
    public Array<ModelInstance> instances = new Array<ModelInstance>(); // array of instances (for loading more models)
    public boolean loading;
    public ModelInstance ball;
    public ModelInstance level1;
    
    
//    private Vector3 lookDir;
//	private Quaternion rotationQuaternion;
//	private float rotationY;
//	public static float totalTime = 0.0f;
//	private boolean moving = false;
//	private int frictionCount = 0;
//	private Matrix4 tmpM4;
//	private boolean firstUpdate = true;

	public void myBox() {
		System.out.println("Creating a Box!");
		// create a simple box by code:
		ModelBuilder modelBuilder = new ModelBuilder();
		model = modelBuilder.createBox(5f, 5f, 5f, // size
				new Material(ColorAttribute.createDiffuse(Color.GREEN)), // material with a green diffuse color
				Usage.Position | Usage.Normal); // add position and normal components to the model
		
		// A ModelInstance contains the location, rotation and scale the model should be rendered at.
		instance = new ModelInstance(model); // default location is at (0,0,0)
	}
	
	@Override
	public void create () {
		System.out.println("create");
//		super.create();
//		
//		lookDir = new Vector3(0, 2, -4); // camera look direction (0, 2, -4)
//		
//		BulletEntity level1 = world.add("world", 0f, 0f, 0f);
//		
//		player = world.add("player", 0f, 0f, 0f);
//		player.motionState.offsetY = 0.18f;
//		
//		rotationQuaternion = new Quaternion();
//		tmpM4 = new Matrix4();
//		
//		Gdx.input.setInputProcessor(new InputMultiplexer(this, new GestureDetector(this))); // to use tap
		
		
		
////////////////////////////// OLD STUFF: ////////////////////////////////
		// lightning:
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		modelBatch = new ModelBatch();

		// setup camera:
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // field of view of 67 degrees (which is common to use) 
		cam.position.set(7f, 7f, 7f); // set the camera 10 units to the right, 10 units up and 10 units back
		cam.lookAt(0,0,0); // set the camera to look at (0,0,0)
		cam.near = 1f; // set the near and far values to make sure we would always see our object
		cam.far = 300f;
		cam.update(); // update the camera so all changes we made are reflected by the camera

//		myBox();
//		ModelLoader loader = new ObjLoader();
//		model = loader.loadModel(Gdx.files.internal("data/ball1.obj"));
		
		// to control the camera:
		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);
		
		assets = new AssetManager();
        assets.load("data/ball1.g3dj", Model.class);
        assets.load("data/level1.g3dj", Model.class);
        loading = true;
//////////////////////////////////////////////////////////////////////////////////
       
	}
	
//	public boolean tap(float x, float y, int count, int button) {
//		System.out.println("shoot " + x +", " + y);
//		shoot(x, y); // shoot box
//		return true;
//	}
//	
//	final float MV_DT = 100.0f;
//
//	private void move(Vector3 mv) {
//		mv.nor().scl(MV_DT * Gdx.graphics.getDeltaTime());
//
//		btRigidBody rb = ((btRigidBody)player.body);
//		Vector3 speed = rb.getLinearVelocity().add(mv);
//		if (speed.len() > 5.0f) {
//			speed.nor().scl(5.0f);
//		}
//
//		rb.setLinearVelocity(speed);
//		rb.activate();
//
//		// enable walking animation:
//		if (!moving) {
//			playerAnimationController.animate(player.modelInstance.animations.get(0).id, -1, null, 0.5f);
//			System.out.println("Animation: " + player.modelInstance.animations.get(0).id);
//			moving = true;
//		}
//	}
//	
//	public void update() {
//		super.update();
//		totalTime += Gdx.graphics.getDeltaTime();
//		
//	}
	
	private void doneLoading() {
        
		ball = new ModelInstance(assets.get("data/ball1.g3dj", Model.class));
		ball.transform.setToScaling(0.2f, 0.2f, 0.2f);
		ball.transform.setTranslation(1f, 5f, 1f);
//		Model ball = assets.get("data/ball1.g3dj", Model.class);
//		ModelInstance ballInstance = new ModelInstance(ball); 
        instances.add(ball);
        
        level1 = new ModelInstance(assets.get("data/level1.g3dj", Model.class));
        instances.add(level1);
  
        
        // many balls test:
//        for (float x = -30f; x <= 30f; x += 15f) {
//            for (float z = -30f; z <= 30f; z += 15f) {
//                ModelInstance ballInstance = new ModelInstance(ball);
//                ballInstance.transform.setToTranslation(x, 0, z);
//                instances.add(ballInstance);
//            }
//        }
        
        loading = false;
    }

	@Override
	public void render () {
		
		if (loading && assets.update())
			doneLoading();

		camController.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT); // clear the screen

		modelBatch.begin(cam);
		//modelBatch.render(instance);
		//modelBatch.render(instance, environment);
		modelBatch.render(instances, environment);
		modelBatch.end();
	}

	@Override
	public void dispose () {
		System.out.println("dispose");
		modelBatch.dispose();
		instances.clear();
		//model.dispose();
		assets.dispose();
	}

	@Override
	public void resume () {
		System.out.println("resume");
	}

	@Override
	public void resize (int width, int height) {
		System.out.println("resize: x: " + width + " y: " + height);
	}

	@Override
	public void pause () {
		System.out.println("pause");
	}
}

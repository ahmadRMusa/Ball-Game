package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btAxisSweep3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btConvexShape;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btGhostPairCallback;
import com.badlogic.gdx.physics.bullet.collision.btPairCachingGhostObject;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;

import sun.java2d.HeadlessGraphicsEnvironment;

public class Game extends BaseBulletTest {

	final int BOXCOUNT_X = 1;
	final int BOXCOUNT_Y = 5;
	final int BOXCOUNT_Z = 40;

	final float BOXOFFSET_X = -19f;
	final float BOXOFFSET_Y = 0.5f;
	final float BOXOFFSET_Z = -20f;

	btGhostPairCallback ghostPairCallback; // else fall through floor
	btPairCachingGhostObject ghostObject;
	btConvexShape ghostShape;
	btKinematicCharacterController characterController;

	Matrix4 characterTransform;
	Vector3 characterDirection = new Vector3();
	Vector3 walkDirection = new Vector3();

	Model playerModel;

	@Override
	// magic stuff going on here:
	public BulletWorld createWorld() {
		// We create the world using an axis sweep broadphase for this test
		btDefaultCollisionConfiguration collisionConfiguration = new btDefaultCollisionConfiguration();
		btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfiguration);
		btAxisSweep3 sweep = new btAxisSweep3(new Vector3(-1000, -1000, -1000), new Vector3(1000, 1000, 1000));
		btSequentialImpulseConstraintSolver solver = new btSequentialImpulseConstraintSolver();
		btDiscreteDynamicsWorld collisionWorld = new btDiscreteDynamicsWorld(dispatcher, sweep, solver, collisionConfiguration);
		ghostPairCallback = new btGhostPairCallback();
		sweep.getOverlappingPairCache().setInternalGhostPairCallback(ghostPairCallback);
		return new BulletWorld(collisionConfiguration, dispatcher, sweep, solver, collisionWorld);
	}

	@Override
	public void create() {
		super.create();

		// Create a visual representation of the character (note that we don't use the physics part of BulletEntity, we'll do that manually)
		final Texture texture = new Texture(Gdx.files.internal("data/ball_texture1.png"));
		disposables.add(texture);
		final Material material = new Material(TextureAttribute.createDiffuse(texture), ColorAttribute.createSpecular(1,1,1,1), FloatAttribute.createShininess(8f));
		final long attributes = Usage.Position | Usage.Normal | Usage.TextureCoordinates;

		playerModel = modelBuilder.createSphere(6f, 6f, 6f, 20, 20, material, attributes);
		disposables.add(playerModel);
		world.addConstructor("playerModel", new BulletConstructor(playerModel, null));
		character = world.add("playerModel", 5f, 3f, 5f); // starting position
		characterTransform = character.transform; // Set by reference

		// Create the physics representation of the character
		ghostObject = new btPairCachingGhostObject();
		ghostObject.setWorldTransform(characterTransform);
		ghostShape = new btSphereShape(3f);
		ghostObject.setCollisionShape(ghostShape);
		ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
		characterController = new btKinematicCharacterController(ghostObject, ghostShape, .35f);

		// And add it to the physics world
		world.collisionWorld.addCollisionObject(ghostObject, 
				(short)btBroadphaseProxy.CollisionFilterGroups.CharacterFilter,
				(short)(btBroadphaseProxy.CollisionFilterGroups.StaticFilter | btBroadphaseProxy.CollisionFilterGroups.DefaultFilter));
		((btDiscreteDynamicsWorld)(world.collisionWorld)).addAction(characterController);

		// Add the ground
		(ground = world.add("ground", 0f, 0f, 0f)).setColor(0.3f, 0.3f, 0.3f, 1f);
		//.setColor(0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 0.25f + 0.5f * (float)Math.random(), 1f);

		// Create some boxes to play with
		createBoxes(world, BOXCOUNT_X, BOXCOUNT_Y, BOXCOUNT_Z);
	}

	public void createBoxes(BulletWorld world, int xn, int yn, int zn) {
		for (int x = 0; x < xn; x++) {
			for (int y = 0; y < yn; y++) {
				for (int z = 0; z < zn; z++) {
					world.add("box", BOXOFFSET_X + x, BOXOFFSET_Y + y, BOXOFFSET_Z + z)
					.setColor(0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 0.5f + 0.5f * (float)Math.random(), 1f);
				}
			}
		}
	}

	// TODO: Rotation around arbitrary axis, using quaternions?
	Quaternion q = new Quaternion();
	boolean debug = false;

	float angle = (float) (5f * (Math.PI / 180));
	Quaternion q2 = new Quaternion();

	public void update() {

		// Fetch which direction the character is facing now  -1,0,0
		//characterDirection.set(0,0,0).rot(characterTransform).nor();
		//walkDirection.set(0,0,0); // Set the walking direction accordingly (either forward or backward)


		if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {

			if (debug)
				characterTransform.rotate(1, 0, 0, 5f); // rotate player (not around a fixed axis though..)

			//q.setFromMatrix(characterTransform);
			characterTransform.getRotation(q);

			//q2.set(q.x * (float)Math.sin(angle/2), q.y * (float)Math.sin(angle/2), q.z * (float)Math.sin(angle/2), (float)Math.cos(angle/2));

			System.out.println("x: " + q.x + " y: " + q.y + " z: " + q.z + " w: " + q.w);
			characterTransform.rotate(q.x, q.y, q.z, 5f);


			ghostObject.setWorldTransform(characterTransform);
			walkDirection.add(0, 0, 1); // move player
		}

		if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {

			if (debug)
				characterTransform.rotate(1, 0, 0, -5f);


			ghostObject.setWorldTransform(characterTransform);
			walkDirection.add(0, 0, -1);
		}

		if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {

			if (debug)
				characterTransform.rotate(0,0,1, 5f);


			ghostObject.setWorldTransform(characterTransform);
			walkDirection.add(-1, 0, 0);
		}

		if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {

			if (debug)
				characterTransform.rotate(0, 0, 1, -5f);


			ghostObject.setWorldTransform(characterTransform);
			walkDirection.add(1, 0, 0);
		}

		walkDirection.scl(10f * Gdx.graphics.getDeltaTime()); // sync?
		characterController.setWalkDirection(walkDirection); // update the character controller
		super.update(); // update the world
		ghostObject.getWorldTransform(characterTransform); // fetch the new transformation of the character


		if (Gdx.input.isKeyJustPressed(Keys.P)) {
			System.out.println("Angle: " + angle);
			System.out.println("Det: " + characterTransform.det());
			System.out.println(characterTransform.toString());
		}

		if (Gdx.input.isKeyJustPressed(Keys.T)) {
			if (debug == false)
				debug = true;
			else
				debug = false;

			System.out.println("debug: " + debug);
		}

		if (Gdx.input.isKeyJustPressed(Keys.R)) {
			System.out.println("Reset");
			//System.out.println("walkDirection: x: " + walkDirection.x + " y: " + walkDirection.y + " z: " + walkDirection.z);
			characterTransform.setToRotation(0,0,0, 0,0,0);
			//characterTransform.setTranslation(0,1,0);
			ghostObject.setWorldTransform(characterTransform);
		}

		if (Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			Gdx.app.exit(); // only for pc?
		}
	}

	@Override
	protected void renderWorld() {
		super.renderWorld();
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		//System.out.println("x = " + x + " y = " + y);
		shoot(x, y);
		return true;
	}

	@Override
	public void dispose() {
		((btDiscreteDynamicsWorld)(world.collisionWorld)).removeAction(characterController);
		world.collisionWorld.removeCollisionObject(ghostObject);
		super.dispose();
		characterController.dispose();
		ghostObject.dispose();
		ghostShape.dispose();
		ghostPairCallback.dispose();
		ground = null;
	}
}




// slask:





//System.out.println("x: " + q.getAxisAngle(new Vector3(1,0,0)) + " y: " + q.getAxisAngle(new Vector3(0,1,0)) + " z: " + q.getAxisAngle(new Vector3(0,0,1)));
//ground.body.getWorldTransform().rotate(1, 0, 0, 5f);
//characterTransform.rotate(new Quaternion(1, 0, 0, 5f));




//public void update() {
//	
//	//characterDirection.set(-1,0,0).rot(characterTransform).nor(); // Fetch which direction the character is facing now
//	//walkDirection.set(0,0,0); // Set the walking direction accordingly (either forward or backward)
//	
//	if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A)) {
//
//		characterTransform.rotate(1, 0, 0, 5f);
//		ghostObject.setWorldTransform(characterTransform);
//		walkDirection.add(0, 0, 1);
//	}
//
//	if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D)) {
//
//		characterTransform.rotate(1, 0, 0, -5f);
//		ghostObject.setWorldTransform(characterTransform);
//		walkDirection.add(0, 0, -1);
//	}
//
//	if (Gdx.input.isKeyPressed(Keys.UP) || Gdx.input.isKeyPressed(Keys.W)) {
//		
//		characterTransform.rotate(0, 0, 1, 5f);
//		ghostObject.setWorldTransform(characterTransform);
//		walkDirection.add(-1, 0, 0);
//	}
//
//	if (Gdx.input.isKeyPressed(Keys.DOWN) || Gdx.input.isKeyPressed(Keys.S)) {
//		
//		characterTransform.rotate(0, 0, 1, -5f);
//		ghostObject.setWorldTransform(characterTransform);
//		walkDirection.add(1, 0, 0);
//	}
//	
//	if (Gdx.input.isKeyPressed(Keys.R)) {
//		System.out.println("walkDirection: x: " + walkDirection.x + " y: " + walkDirection.y + " z: " + walkDirection.z);
//		//characterDirection.set(0, 0, 0);
//		walkDirection.set(0,0,0);
//		//character.transform.setTranslation(0, 0, 0);
//		//character.transform.rotate(1,0,0, 5f);
//	}
//
//	walkDirection.scl(10f * Gdx.graphics.getDeltaTime()); // ???
//	characterController.setWalkDirection(walkDirection); // update the character controller
//	super.update(); // update the world
//	ghostObject.getWorldTransform(characterTransform); // fetch the new transformation of the character
//}






//if (Gdx.input.isKeyJustPressed(Keys.P)) {
//	System.out.println("Gravity: " + characterController.getGravity());
//	System.out.println("Det: " + characterTransform.det());
//	System.out.println(characterTransform.M00 + "\t" + characterTransform.M01 + "\t" + characterTransform.M02 + "\t" + characterTransform.M03);
//	System.out.println(characterTransform.M10 + "\t" + characterTransform.M11 + "\t" + characterTransform.M12 + "\t" + characterTransform.M13);
//	System.out.println(characterTransform.M20 + "\t" + characterTransform.M21 + "\t" + characterTransform.M22 + "\t" + characterTransform.M23);
//	System.out.println(characterTransform.M30 + "\t" + characterTransform.M31 + "\t" + characterTransform.M32 + "\t" + characterTransform.M33 + "\n\n");
//	System.out.println(characterTransform.toString());
//}


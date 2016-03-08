package com.mygdx.game;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Disposable;

/** @author xoppa Renderable BaseEntity with a bullet physics body. */
public class BulletEntity extends BaseEntity {
	private final static Matrix4 tmpM = new Matrix4();
	public BulletEntity.MotionState motionState;
	public btCollisionObject body;

	public BulletEntity(final Model model, final btRigidBodyConstructionInfo bodyInfo, final float x, final float y, final float z) {
		this(model, bodyInfo == null ? null : new btRigidBody(bodyInfo), x, y, z);
	}

	public BulletEntity(final Model model, final btRigidBodyConstructionInfo bodyInfo, final Matrix4 transform) {
		this(model, bodyInfo == null ? null : new btRigidBody(bodyInfo), transform);
	}

	public BulletEntity(final Model model, final btCollisionObject body, final float x, final float y, final float z) {
		this(model, body, tmpM.setToTranslation(x, y, z));
	}

	public BulletEntity(final Model model, final btCollisionObject body, final Matrix4 transform) {
		this(new ModelInstance(model, transform.cpy()), body);
	}

	public BulletEntity(final ModelInstance modelInstance, final btCollisionObject body) {
		this.modelInstance = modelInstance;
		this.transform = this.modelInstance.transform;
		this.body = body;

		if (body != null) {
			body.userData = this;
			if (body instanceof btRigidBody) {
				this.motionState = new MotionState(this.modelInstance.transform);
				((btRigidBody)this.body).setMotionState(motionState);
			} else
				body.setWorldTransform(transform);
		}
	}
	
	public void rotate(float axisX, float axisY, float axisZ, float degrees) {
		Matrix4 trans = body.getWorldTransform();
		trans.rotate(axisX, axisY, axisZ, degrees);
		
		body.setWorldTransform( trans );
        motionState.setWorldTransform( trans );
	}

	@Override
	public void dispose() {
		// Don't rely on the GC
		if (motionState != null) motionState.dispose();
		if (body != null) body.dispose();
		// And remove the reference
		motionState = null;
		body = null;
	}

	static class MotionState extends btMotionState {
		private final Matrix4 transform;
		public float offsetY;

		public MotionState (final Matrix4 transform) {
			this.transform = transform;
		}

		/** For dynamic and static bodies this method is called by bullet once to get the initial state of the body. For kinematic
		 * bodies this method is called on every update, unless the body is deactivated. */
		@Override
		public void getWorldTransform(final Matrix4 worldTrans) {
			worldTrans.set(transform);
			worldTrans.translate(0f, offsetY, 0f);
		}

		/** For dynamic bodies this method is called by bullet every update to inform about the new position and rotation. */
		@Override
		public void setWorldTransform (final Matrix4 worldTrans) {
			transform.set(worldTrans);
			transform.translate(0f, -offsetY, 0f);
		}
	}
}

package io.narayana.sra.tests;

import java.lang.annotation.Annotation;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import io.narayana.sra.annotation.SRA;

public class SRACreatorTest implements SRA {

	@Override
	public Class<? extends Annotation> annotationType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type value() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean enableJTABridge() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delayCommit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean terminal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Family[] cancelOnFamily() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status[] cancelOn() {
		// TODO Auto-generated method stub
		return null;
	}

}

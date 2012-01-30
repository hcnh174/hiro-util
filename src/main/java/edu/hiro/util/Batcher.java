package edu.hiro.util;


public abstract class Batcher
{
	protected int batchsize;
	protected int total;
	protected int delay;
	protected int numbatches;

	public Batcher(int batchsize, int total, int delay)
	{
		this.batchsize=batchsize;
		this.total=total;
		this.delay=delay;
		this.numbatches=MathHelper.getNumbatches(total,batchsize);
	}
	
	public void doInBatches()
	{
		for (int batchnumber=0;batchnumber<numbatches;batchnumber++)
		{
			int fromIndex=batchnumber*this.batchsize;
			int toIndex=fromIndex+this.batchsize;
			if (toIndex>=this.total)
				toIndex=this.total;
			System.out.println("batch load ids - from "+fromIndex+" to "+toIndex);
			doBatch(fromIndex,toIndex,batchnumber);
			if (batchnumber<this.numbatches-1)
				ThreadHelper.sleep(this.delay);
		}
	}
	
	protected abstract void doBatch(int fromIndex, int toIndex, int batchnumber);
}
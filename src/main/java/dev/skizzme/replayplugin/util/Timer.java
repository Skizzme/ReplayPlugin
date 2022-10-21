package dev.skizzme.replayplugin.util;

public class Timer {
	
	public long lastMS = System.currentTimeMillis();
	public long lastNano = System.nanoTime();
	
	public void reset() {
		lastMS = System.currentTimeMillis();
	}

	public boolean hasTimeElapsed(long time, boolean reset) {
		if(System.currentTimeMillis()-lastMS > time) {
			if (reset)
				reset();

			return true;
		}
		return false;
	}

	public void resetNano() {
		lastNano = System.nanoTime();
	}

	public boolean hasNanoElapsed(long time, boolean reset) {
		if(System.nanoTime()-lastNano > time) {
			if (reset)
				reset();

			return true;
		}
		return false;
	}

	public boolean hasNanoElapsedMilli(long time, boolean reset) {
		if (System.nanoTime()-lastNano>time*1000000f) {
			if (reset)
				reset();

			return true;
		}
		return false;
	}

	public long getNanoDelay() {
		return System.nanoTime()-lastNano;
	}

	public double getNanoInMiliDelay() {
		return (System.nanoTime()-lastNano)/1000000f;
	}
	
	public int getDelay() {
		return (int) (System.currentTimeMillis() - this.lastMS);
	}
	
}

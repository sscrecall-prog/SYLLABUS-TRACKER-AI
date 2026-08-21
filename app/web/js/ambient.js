/* WebAudio Ambient Sound Synthesizer */

class AmbientSynthesizer {
  constructor() {
    this.audioCtx = null;
    this.currentType = 'NONE';
    this.isPlaying = false;
    this.volume = 0.5;
    this.masterGain = null;
    this.activeNodes = [];
    this.autoSyncTimer = true;
  }

  init() {
    if (!this.audioCtx) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (AudioContext) {
        this.audioCtx = new AudioContext();
        this.masterGain = this.audioCtx.createGain();
        this.masterGain.gain.value = this.volume;
        this.masterGain.connect(this.audioCtx.destination);
      }
    }
  }

  setVolume(val) {
    this.volume = Math.max(0, Math.min(1, val));
    if (this.masterGain) {
      this.masterGain.gain.setTargetAtTime(this.volume, this.audioCtx.currentTime, 0.1);
    }
  }

  setSound(type) {
    this.currentType = type;
    if (this.isPlaying) {
      this.stop();
      if (type !== 'NONE') {
        this.play(type);
      }
    }
  }

  play(type = this.currentType) {
    this.init();
    if (!this.audioCtx) return;

    if (this.audioCtx.state === 'suspended') {
      this.audioCtx.resume();
    }

    this.stop();
    this.currentType = type;

    if (type === 'NONE') {
      this.isPlaying = false;
      return;
    }

    this.isPlaying = true;

    if (type === 'RAIN') {
      this._createRainSound();
    } else if (type === 'OCEAN') {
      this._createOceanSound();
    } else if (type === 'FOREST') {
      this._createForestSound();
    } else if (type === 'WHITE_NOISE') {
      this._createWhiteNoiseSound();
    }
  }

  stop() {
    this.activeNodes.forEach(n => {
      try {
        if (n.stop) n.stop();
        if (n.disconnect) n.disconnect();
      } catch (e) {}
    });
    this.activeNodes = [];
    this.isPlaying = false;
  }

  togglePlayPause() {
    if (this.isPlaying) {
      this.stop();
    } else {
      if (this.currentType === 'NONE') {
        this.currentType = 'RAIN';
      }
      this.play(this.currentType);
    }
    return this.isPlaying;
  }

  /* Internal Audio Node Generators */

  _createNoiseBuffer(seconds = 3) {
    const bufferSize = this.audioCtx.sampleRate * seconds;
    const buffer = this.audioCtx.createBuffer(1, bufferSize, this.audioCtx.sampleRate);
    const data = buffer.getChannelData(0);
    for (let i = 0; i < bufferSize; i++) {
      data[i] = Math.random() * 2 - 1;
    }
    return buffer;
  }

  _createRainSound() {
    const noise = this.audioCtx.createBufferSource();
    noise.buffer = this._createNoiseBuffer();
    noise.loop = true;

    const filter = this.audioCtx.createBiquadFilter();
    filter.type = 'lowpass';
    filter.frequency.value = 1000;

    const gain = this.audioCtx.createGain();
    gain.gain.value = 0.4;

    noise.connect(filter);
    filter.connect(gain);
    gain.connect(this.masterGain);

    noise.start();
    this.activeNodes.push(noise, filter, gain);
  }

  _createOceanSound() {
    const noise = this.audioCtx.createBufferSource();
    noise.buffer = this._createNoiseBuffer();
    noise.loop = true;

    const filter = this.audioCtx.createBiquadFilter();
    filter.type = 'lowpass';
    filter.frequency.value = 400;

    const gain = this.audioCtx.createGain();
    gain.gain.value = 0.5;

    // LFO to simulate wave cycles
    const lfo = this.audioCtx.createOscillator();
    lfo.frequency.value = 0.12; // 12 second wave cycle
    const lfoGain = this.audioCtx.createGain();
    lfoGain.gain.value = 0.3;

    lfo.connect(lfoGain);
    lfoGain.connect(gain.gain);

    noise.connect(filter);
    filter.connect(gain);
    gain.connect(this.masterGain);

    noise.start();
    lfo.start();
    this.activeNodes.push(noise, filter, gain, lfo, lfoGain);
  }

  _createForestSound() {
    const noise = this.audioCtx.createBufferSource();
    noise.buffer = this._createNoiseBuffer();
    noise.loop = true;

    const filter = this.audioCtx.createBiquadFilter();
    filter.type = 'bandpass';
    filter.frequency.value = 800;
    filter.Q.value = 3;

    const gain = this.audioCtx.createGain();
    gain.gain.value = 0.2;

    noise.connect(filter);
    filter.connect(gain);
    gain.connect(this.masterGain);

    noise.start();
    this.activeNodes.push(noise, filter, gain);
  }

  _createWhiteNoiseSound() {
    const noise = this.audioCtx.createBufferSource();
    noise.buffer = this._createNoiseBuffer();
    noise.loop = true;

    const gain = this.audioCtx.createGain();
    gain.gain.value = 0.25;

    noise.connect(gain);
    gain.connect(this.masterGain);

    noise.start();
    this.activeNodes.push(noise, gain);
  }
}

export const ambientAudio = new AmbientSynthesizer();

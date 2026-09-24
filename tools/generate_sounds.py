"""
Membuat semua efek suara dan musik game secara sintetis (tanpa aset berhak cipta).
Output: sound/<nama>.wav (mono, 22050 Hz, 16-bit).

Jalankan dari root project:  python tools/generate_sounds.py
Butuh numpy.
"""
import os
import wave

import numpy as np

RATE = 22050
OUT = "sound"
rng = np.random.default_rng(7)


def t(duration):
    return np.arange(int(RATE * duration)) / RATE


def env(n, attack=0.005, release=None, curve=4.0):
    """Envelope: attack linear lalu decay eksponensial."""
    e = np.ones(n)
    a = max(1, int(RATE * attack))
    e[:a] = np.linspace(0, 1, a)
    decay = np.linspace(0, 1, n - a)
    e[a:] = np.exp(-curve * decay)
    if release:
        r = int(RATE * release)
        e[-r:] *= np.linspace(1, 0, r)
    return e


def lowpass(x, alpha):
    y = np.zeros_like(x)
    acc = 0.0
    for i, v in enumerate(x):
        acc += alpha * (v - acc)
        y[i] = acc
    return y


def noise(n):
    return rng.uniform(-1, 1, n)


def sine(freq, tt):
    return np.sin(2 * np.pi * np.cumsum(np.broadcast_to(freq, tt.shape)) / RATE)


def square(freq, tt, duty=0.5):
    phase = np.cumsum(np.broadcast_to(freq, tt.shape)) / RATE % 1.0
    return np.where(phase < duty, 1.0, -1.0)


def saw(freq, tt):
    phase = np.cumsum(np.broadcast_to(freq, tt.shape)) / RATE % 1.0
    return 2 * phase - 1


def save(name, x, volume=0.8):
    x = np.asarray(x, dtype=np.float64)
    peak = np.max(np.abs(x)) or 1.0
    x = x / peak * volume
    data = (x * 32767).astype(np.int16)
    os.makedirs(OUT, exist_ok=True)
    with wave.open(os.path.join(OUT, name + ".wav"), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(data.tobytes())
    print(f"  {name:12s} {len(x) / RATE:5.2f} s")


# ---------------------------------------------------------------- efek suara

def sfx():
    tt = t(0.18)  # tanam: "duk" rendah + sedikit tanah
    save("plant", sine(np.linspace(180, 70, len(tt)), tt) * env(len(tt), curve=6)
         + 0.3 * lowpass(noise(len(tt)), 0.15) * env(len(tt), curve=9))

    tt = t(0.09)  # tembak: "pop"
    save("shoot", sine(np.linspace(900, 320, len(tt)), tt) * env(len(tt), 0.002, curve=5), 0.6)

    tt = t(0.12)  # kena: "splat"
    save("hit", lowpass(noise(len(tt)), 0.35) * env(len(tt), 0.001, curve=7)
         + 0.4 * sine(np.linspace(260, 90, len(tt)), tt) * env(len(tt), curve=8), 0.55)

    tt = t(0.45)  # ambil sun: dua nada "ting"
    a = sine(1318.5, tt) * env(len(tt), curve=5)
    b = np.concatenate([np.zeros(int(RATE * 0.07)), (sine(1760, tt) * env(len(tt), curve=5))[:len(tt) - int(RATE * 0.07)]])
    save("sun", a + 0.8 * b, 0.55)

    tt = t(1.1)  # ledakan
    boom = lowpass(noise(len(tt)), 0.06) * env(len(tt), 0.003, curve=3.5)
    save("explode", boom + 0.6 * sine(np.linspace(90, 30, len(tt)), tt) * env(len(tt), curve=4), 0.95)

    tt = t(0.5)  # potato mine: "spudow" pendek
    save("mine", lowpass(noise(len(tt)), 0.12) * env(len(tt), 0.002, curve=5)
         + 0.7 * sine(np.linspace(150, 45, len(tt)), tt) * env(len(tt), curve=5), 0.9)

    tt = t(0.9)  # api jalapeno: whoosh
    whoosh = lowpass(noise(len(tt)), np.linspace(0.05, 0.4, len(tt)).mean()) * np.sin(np.pi * tt / tt[-1])
    save("fire", whoosh * (0.6 + 0.4 * np.sin(2 * np.pi * 9 * tt)), 0.8)

    tt = t(1.3)  # mesin lawn mower
    engine = saw(62 + 6 * np.sin(2 * np.pi * 3 * tt), tt) * (0.7 + 0.3 * square(18, tt))
    save("mower", lowpass(engine, 0.25) * env(len(tt), 0.05, release=0.25, curve=0.6), 0.6)

    tt = t(0.14)  # gigitan zombie
    click = lowpass(noise(len(tt)), 0.5) * env(len(tt), 0.001, curve=14)
    second = np.concatenate([np.zeros(int(RATE * 0.06)), click[:len(tt) - int(RATE * 0.06)]])
    save("chomp", click + 0.8 * second, 0.5)

    tt = t(0.35)  # tanaman habis dimakan: "gulp"
    save("gulp", sine(np.linspace(300, 110, len(tt)), tt) * env(len(tt), 0.01, curve=4), 0.6)

    tt = t(1.2)  # erangan zombie: gelombang saw rendah dengan vibrato, difilter
    pitch = 95 + 12 * np.sin(2 * np.pi * 4.5 * tt) - 25 * tt
    groan = lowpass(saw(pitch, tt), 0.08) + 0.4 * lowpass(saw(pitch * 1.5, tt), 0.05)
    save("groan", groan * np.sin(np.pi * tt / tt[-1]) ** 0.7, 0.5)

    tt = t(1.6)  # sirene huge wave
    save("siren", square(520 + 260 * np.abs(np.sin(2 * np.pi * 1.1 * tt)), tt, 0.4)
         * env(len(tt), 0.05, release=0.3, curve=0.4), 0.35)

    tt = t(1.4)  # final wave: nada turun berat
    save("finalwave", (saw(np.linspace(220, 55, len(tt)), tt) + 0.5 * lowpass(noise(len(tt)), 0.05))
         * env(len(tt), 0.02, curve=2), 0.5)

    tt = t(0.1)  # sekop
    save("dig", lowpass(noise(len(tt)), 0.25) * env(len(tt), 0.001, curve=6), 0.5)

    tt = t(0.05)  # klik tombol
    save("click", sine(1200, tt) * env(len(tt), 0.001, curve=10), 0.4)

    notes = [523.25, 659.25, 783.99, 1046.5]  # menang: arpeggio mayor
    save("win", np.concatenate([square(f, t(0.16), 0.3) * env(len(t(0.16)), curve=2) for f in notes]
                               + [square(1046.5, t(0.6), 0.3) * env(len(t(0.6)), curve=2.5)]), 0.35)

    notes = [392.0, 349.23, 311.13, 261.63]  # kalah: turun minor
    save("lose", np.concatenate([saw(f, t(0.3)) * env(len(t(0.3)), curve=1.5) for f in notes]
                                + [saw(196.0, t(0.9)) * env(len(t(0.9)), curve=2)]), 0.35)


# ---------------------------------------------------------------- musik (loop)

def note_freq(name):
    names = {"C": -9, "C#": -8, "D": -7, "D#": -6, "E": -5, "F": -4, "F#": -3, "G": -2, "G#": -1, "A": 0, "A#": 1,
             "B": 2}
    pitch, octave = name[:-1], int(name[-1])
    return 440.0 * 2 ** ((names[pitch] + 12 * (octave - 4)) / 12)


def render_track(melody, bass, bpm, lead_wave, bass_wave):
    beat = 60.0 / bpm
    total_beats = sum(d for _, d in melody)
    out = np.zeros(int(RATE * beat * total_beats) + RATE)

    def place(seq, wave_fn, gain, curve):
        pos = 0.0
        for name, dur in seq:
            n = int(RATE * beat * dur)
            if name != "-":
                tt = t(beat * dur)
                x = wave_fn(note_freq(name), tt) * env(len(tt), 0.01, release=0.02, curve=curve)
                start = int(RATE * beat * pos)
                out[start:start + n] += gain * x[:n]
            pos += dur

    place(melody, lead_wave, 0.5, 2.2)
    place(bass, bass_wave, 0.45, 1.2)
    return out[:int(RATE * beat * total_beats)]


def music():
    # Melodi orisinal yang ceria untuk siang hari
    day_melody = [("E5", 0.5), ("G5", 0.5), ("A5", 1), ("G5", 0.5), ("E5", 0.5), ("D5", 1),
                  ("C5", 0.5), ("D5", 0.5), ("E5", 0.5), ("G5", 0.5), ("E5", 2),
                  ("A5", 0.5), ("C6", 0.5), ("B5", 1), ("A5", 0.5), ("G5", 0.5), ("E5", 1),
                  ("D5", 0.5), ("E5", 0.5), ("D5", 0.5), ("B4", 0.5), ("C5", 2)] * 2
    day_bass = [("C3", 1), ("G3", 1), ("C3", 1), ("G3", 1), ("A2", 1), ("E3", 1), ("A2", 1), ("E3", 1),
                ("F2", 1), ("C3", 1), ("F2", 1), ("C3", 1), ("G2", 1), ("D3", 1), ("G2", 1), ("B2", 1)] * 2
    square_lead = lambda f, tt: 0.6 * square(f, tt, 0.25) + 0.4 * sine(f, tt)
    triangle = lambda f, tt: 2 * np.abs(saw(f, tt)) - 1
    save("music_day", render_track(day_melody, day_bass, 132, square_lead, triangle), 0.45)

    # Melodi orisinal yang misterius untuk malam hari
    night_melody = [("A4", 1), ("C5", 0.5), ("E5", 0.5), ("D#5", 1), ("E5", 1),
                    ("F5", 0.5), ("E5", 0.5), ("D5", 0.5), ("C5", 0.5), ("B4", 2),
                    ("A4", 1), ("C5", 0.5), ("E5", 0.5), ("G5", 1), ("F5", 1),
                    ("E5", 0.5), ("D5", 0.5), ("C5", 0.5), ("B4", 0.5), ("A4", 2)] * 2
    night_bass = [("A2", 2), ("E2", 2), ("F2", 2), ("E2", 2), ("A2", 2), ("C3", 2), ("D3", 2), ("E2", 2)] * 2
    soft_lead = lambda f, tt: sine(f, tt) + 0.3 * sine(2 * f, tt)
    save("music_night", render_track(night_melody, night_bass, 100, soft_lead, triangle), 0.45)


if __name__ == "__main__":
    print("efek suara:")
    sfx()
    print("musik:")
    music()

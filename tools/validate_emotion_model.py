import argparse
from pathlib import Path
import numpy as np
import tensorflow as tf
from PIL import Image

LABELS = ["Angry", "Fear", "Happy", "Sad", "Surprise", "Neutral"]


def preprocess_image(path: Path):
    image = Image.open(path).convert("L").resize((48, 48))
    array = np.asarray(image, dtype=np.float32) / 255.0
    array = np.expand_dims(array, axis=(0, -1))
    return array


def predict_keras(model_path: Path, image_array: np.ndarray):
    model = tf.keras.models.load_model(model_path, compile=False)
    output = model.predict(image_array, verbose=0)[0]
    index = int(np.argmax(output))
    return LABELS[index], float(output[index]), output


def predict_tflite(model_path: Path, image_array: np.ndarray):
    interpreter = tf.lite.Interpreter(model_path=str(model_path))
    interpreter.allocate_tensors()
    input_details = interpreter.get_input_details()[0]
    output_details = interpreter.get_output_details()[0]
    interpreter.set_tensor(input_details["index"], image_array.astype(np.float32))
    interpreter.invoke()
    output = interpreter.get_tensor(output_details["index"])[0]
    index = int(np.argmax(output))
    return LABELS[index], float(output[index]), output


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("image_path")
    parser.add_argument(
        "--keras-model",
        default=r"C:\Users\Pc\Desktop\Deni\info\an 3\licenta\model\emotion_model_finetuned_final.keras"
    )
    parser.add_argument(
        "--tflite-model",
        default=r"C:\Users\Pc\AndroidStudioProjects\EmotionTherapyApp\app\src\main\assets\emotion_model.tflite"
    )
    args = parser.parse_args()

    image_path = Path(args.image_path)
    image_array = preprocess_image(image_path)

    keras_label, keras_conf, keras_output = predict_keras(Path(args.keras_model), image_array)
    tflite_label, tflite_conf, tflite_output = predict_tflite(Path(args.tflite_model), image_array)

    print(f"IMAGE: {image_path}")
    print(f"KERAS:  {keras_label} ({keras_conf:.4f})")
    print(f"TFLITE: {tflite_label} ({tflite_conf:.4f})")
    print("KERAS RAW:", np.array2string(keras_output, precision=4))
    print("TFLITE RAW:", np.array2string(tflite_output, precision=4))


if __name__ == "__main__":
    main()

import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  Image,
  ScrollView,
  Platform,
} from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import axios from 'axios';
import { router } from 'expo-router';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {API_URL} from '../../constants/config';

export default function AddMedicationScreen() {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    seuil: '',
    sellPrice: '',
  });
  const [image, setImage] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const pickImage = async () => {
    try {
      const result = await ImagePicker.launchImageLibraryAsync({
        mediaTypes: ImagePicker.MediaTypeOptions.Images,
        allowsEditing: true,
        aspect: [4, 3],
        quality: 0.8,
      });

      if (!result.canceled) {
        setImage(result.assets[0].uri);
      }
    } catch (error) {
      console.error('Erreur lors de la sélection de l\'image:', error);
      Alert.alert('Erreur', 'Impossible de sélectionner l\'image');
    }
  };

  const takePhoto = async () => {
    try {
      const { status } = await ImagePicker.requestCameraPermissionsAsync();
      if (status !== 'granted') {
        Alert.alert('Erreur', 'Permission d\'accès à la caméra refusée');
        return;
      }

      const result = await ImagePicker.launchCameraAsync({
        allowsEditing: true,
        aspect: [4, 3],
        quality: 0.8,
      });

      if (!result.canceled) {
        setImage(result.assets[0].uri);
      }
    } catch (error) {
      console.error('Erreur lors de la prise de photo:', error);
      Alert.alert('Erreur', 'Impossible de prendre une photo');
    }
  };

  const handleSubmit = async () => {
    try {
      if (!image) {
        Alert.alert('Erreur', 'Veuillez ajouter une photo du médicament');
        return;
      }

      if (!formData.seuil || !formData.sellPrice) {
        Alert.alert('Erreur', 'Veuillez remplir tous les champs');
        return;
      }

      setIsLoading(true);

      const pharmacyData = await AsyncStorage.getItem('pharmacyData');
      if (!pharmacyData) {
        throw new Error('Données de la pharmacie non trouvées');
      }

      const pharmacy = JSON.parse(pharmacyData);
      const pharmacyId = pharmacy.id;

      const formDataToSend = new FormData();
      formDataToSend.append('name', formData.name);
      formDataToSend.append('description', formData.description);
      formDataToSend.append('pharmacyId', pharmacyId);
      formDataToSend.append('seuil', formData.seuil);
      formDataToSend.append('sellPrice', formData.sellPrice);

      let imageUri = image;
      if (Platform.OS === 'web') {
        const response = await fetch(image);
        const blob = await response.blob();
        formDataToSend.append('image', blob, 'medication.jpg');
      } else {
        imageUri = Platform.OS === 'android' ? image : image.replace('file://', '');
        const imageName = imageUri.split('/').pop() || 'medication.jpg';
        const imageType = `image/${imageName.split('.').pop() || 'jpg'}`;

        formDataToSend.append('image', {
          uri: imageUri,
          type: imageType,
          name: imageName
        } as any);
      }

      const token = await AsyncStorage.getItem('token');
      if (!token) {
        throw new Error('Token non trouvé');
      }

      console.log('Envoi de la requête...');
      const response = await axios.post(
        `${API_URL}/api/medications/add`,
        formDataToSend,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': `Bearer ${token}`,
          },
          timeout: 30000,
        }
      );

      console.log('Réponse reçue:', response.data);
      Alert.alert('Succès', 'Médicament ajouté avec succès !');
      setFormData({
        name: '',
        description: '',
        seuil: '',
        sellPrice: '',
      });
      setImage(null);
    } catch (error) {
      console.error('Erreur détaillée:', error);
      if (axios.isAxiosError(error)) {
        console.error('Réponse d\'erreur:', error.response?.data);
        Alert.alert(
          'Erreur',
          error.response?.data?.message || 'Une erreur est survenue lors de l\'ajout du médicament'
        );
      } else {
        Alert.alert('Erreur', 'Une erreur inattendue est survenue');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.formContainer}>
        <Text style={styles.title}>Ajouter un médicament</Text>

        <TextInput
          style={styles.input}
          placeholder="Nom du médicament"
          value={formData.name}
          onChangeText={(text) => setFormData({ ...formData, name: text })}
        />

        <TextInput
          style={[styles.input, styles.textArea]}
          placeholder="Description"
          multiline
          numberOfLines={4}
          value={formData.description}
          onChangeText={(text) => setFormData({ ...formData, description: text })}
        />

        <TextInput
          style={styles.input}
          placeholder="Seuil d'alerte (quantité minimale en stock)"
          keyboardType="numeric"
          value={formData.seuil}
          onChangeText={(text) => setFormData({ ...formData, seuil: text })}
        />

        <TextInput
          style={styles.input}
          placeholder="Prix de vente (en €)"
          keyboardType="numeric"
          value={formData.sellPrice}
          onChangeText={(text) => setFormData({ ...formData, sellPrice: text })}
        />

        <View style={styles.imageContainer}>
          {image ? (
            <Image source={{ uri: image }} style={styles.image} />
          ) : (
            <View style={styles.imagePlaceholder}>
              <Text style={styles.imagePlaceholderText}>Aucune image</Text>
            </View>
          )}
        </View>

        <View style={styles.buttonContainer}>
          <TouchableOpacity style={styles.button} onPress={pickImage}>
            <Text style={styles.buttonText}>Choisir une photo</Text>
          </TouchableOpacity>

          {Platform.OS !== 'web' && (
            <TouchableOpacity style={styles.button} onPress={takePhoto}>
              <Text style={styles.buttonText}>Prendre une photo</Text>
            </TouchableOpacity>
          )}
        </View>

        <TouchableOpacity 
          style={[styles.submitButton, isLoading && styles.submitButtonDisabled]} 
          onPress={handleSubmit}
          disabled={isLoading}
        >
          <Text style={styles.submitButtonText}>
            {isLoading ? 'Envoi en cours...' : 'Ajouter le médicament'}
          </Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  formContainer: {
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 20,
    textAlign: 'center',
  },
  input: {
    backgroundColor: '#f5f5f5',
    padding: 15,
    borderRadius: 10,
    marginBottom: 15,
    fontSize: 16,
  },
  textArea: {
    height: 100,
    textAlignVertical: 'top',
  },
  imageContainer: {
    marginVertical: 20,
    alignItems: 'center',
  },
  image: {
    width: 200,
    height: 200,
    borderRadius: 10,
  },
  imagePlaceholder: {
    width: 200,
    height: 200,
    backgroundColor: '#f5f5f5',
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  imagePlaceholderText: {
    color: '#666',
    fontSize: 16,
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: 20,
  },
  button: {
    backgroundColor: '#007AFF',
    padding: 15,
    borderRadius: 10,
    flex: 1,
    marginHorizontal: 5,
  },
  buttonText: {
    color: '#fff',
    textAlign: 'center',
    fontSize: 16,
    fontWeight: 'bold',
  },
  submitButton: {
    backgroundColor: '#34C759',
    padding: 15,
    borderRadius: 10,
    marginTop: 10,
  },
  submitButtonDisabled: {
    backgroundColor: '#ccc',
  },
  submitButtonText: {
    color: '#fff',
    textAlign: 'center',
    fontSize: 16,
    fontWeight: 'bold',
  },
}); 
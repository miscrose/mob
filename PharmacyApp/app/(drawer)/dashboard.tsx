import React from 'react';
import { View, Text, Image, StyleSheet } from 'react-native';
import { API_URL } from '../../constants/config';

export default function Dashboard() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Test d'affichage d'images</Text>
      
      <View style={styles.imageContainer}>
        <Text>Test image 1:</Text>
        <Image 
          source={{ uri: `${API_URL}/uploads/medications/pharmacy_4/a.jpeg` }}
          style={styles.image}
          onError={(e) => console.log('Erreur image 1:', e.nativeEvent.error)}
        />
      </View>

      <View style={styles.imageContainer}>
        <Text>Test image 2:</Text>
        <Image 
          source={{ uri: `${API_URL}/uploads/medications/test.jpg` }}
          style={styles.image}
          onError={(e) => console.log('Erreur image 2:', e.nativeEvent.error)}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  imageContainer: {
    marginBottom: 20,
  },
  image: {
    width: 200,
    height: 200,
    marginTop: 10,
  },
}); 
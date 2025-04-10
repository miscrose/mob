import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export default function VenteScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Vente</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#fff',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
  },
}); 
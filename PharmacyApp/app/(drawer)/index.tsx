import { Redirect } from 'expo-router';
import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

export default function HomeScreen() {
  return (
 /*   <View style={styles.container}>
      <Text style={styles.title}>Tableau de bord,,,</Text>
    </View>*/


<Redirect href="/dashboard" />


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
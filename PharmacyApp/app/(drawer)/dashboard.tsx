import { View, Text, StyleSheet } from 'react-native';
import { useEffect, useState } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';

interface PharmacyData {
  id: number;
  name: string;
  email: string;
  address: string;
  phone: string;
}

export default function Dashboard() {
  const [pharmacyName, setPharmacyName] = useState<string>('');

  useEffect(() => {
    const getPharmacyData = async () => {
      try {
        const pharmacyData = await AsyncStorage.getItem('pharmacyData');
        if (pharmacyData) {
          const data: PharmacyData = JSON.parse(pharmacyData);
          setPharmacyName(data.name);
        }
      } catch (error) {
        console.error('Erreur lors de la récupération des données:', error);
      }
    };

    getPharmacyData();
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Tableau de bord - {pharmacyName}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    fontSize: 20,
    fontWeight: 'bold',
  },
}); 
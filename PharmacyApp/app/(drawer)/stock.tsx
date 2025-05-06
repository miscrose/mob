import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, TextInput, TouchableOpacity, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useFocusEffect } from '@react-navigation/native';
import MedicationCard from '../components/MedicationCard';
import { MedicationStock } from '../../constants/types/stock';
import axios from 'axios';
import { API_URL } from '../../constants/config';
import AsyncStorage from '@react-native-async-storage/async-storage';

export default function StockScreen() {
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState<string | null>(null);
  const [medications, setMedications] = useState<MedicationStock[]>([]);
  const [loading, setLoading] = useState(true);
  const [pharmacyId, setPharmacyId] = useState<number | null>(null);
  const router = useRouter();
  const [token, setToken] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const loadPharmacyData = useCallback(async () => {
    try {
      const [pharmacyData, storedToken] = await Promise.all([
        AsyncStorage.getItem('pharmacyData'),
        AsyncStorage.getItem('token')
      ]);

      if (pharmacyData && storedToken) {
        setToken(storedToken);
        const pharmacy = JSON.parse(pharmacyData);
        setPharmacyId(pharmacy.id);
        return { id: pharmacy.id, token: storedToken };
      }
      return null;
    } catch (error) {
      console.error('Error loading pharmacy data:', error);
      return null;
    }
  }, []);

  const fetchMedications = useCallback(async (id: number, authToken: string) => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_URL}/api/stock/pharmacy/${id}`, {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${authToken}`
        },
      });
      setMedications(response.data);
      setError(null);
    } catch (error) {
      console.error('Error fetching medications:', error);
      setError('Erreur lors du chargement des médicaments');
    } finally {
      setLoading(false);
    }
  }, []);

  const loadData = useCallback(async () => {
    const data = await loadPharmacyData();
    if (data) {
      await fetchMedications(data.id, data.token);
    }
  }, [loadPharmacyData, fetchMedications]);

  // Chargement initial
  useEffect(() => {
    loadData();
  }, [loadData]);

  // Rechargement à chaque focus
  useFocusEffect(
    useCallback(() => {
      loadData();
    }, [loadData])
  );

  const filteredMedications = medications.filter(med => {
    const matchesSearch = med.name.toLowerCase().includes(searchQuery.toLowerCase());
    if (!activeFilter) return matchesSearch;
    
    if (activeFilter === 'low') {
      return matchesSearch && med.totalQuantity < med.seuil;
    }
    if (activeFilter === 'expiring') {
      return matchesSearch && med.lots.some(lot => {
        const expirationDate = new Date(lot.expirationDate);
        const today = new Date();
        const diffTime = expirationDate.getTime() - today.getTime();
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays < 30;
      });
    }
    return matchesSearch;
  });

  if (loading) {
    return (
      <View style={styles.container}>
        <ActivityIndicator size="large" color="#0000ff" />
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.container}>
        <Text style={styles.error}>{error}</Text>
        <TouchableOpacity onPress={() => loadData()} style={styles.retryButton}>
          <Text style={styles.retryButtonText}>Réessayer</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      {/* En-tête */}
      <View style={styles.header}>
        <Text style={styles.title}>Gestion du Stock</Text>
        <TouchableOpacity 
          style={styles.addButton}
          onPress={() => router.push('/add-medication')}
        >
          <Ionicons name="add-circle" size={24} color="#007AFF" />
        </TouchableOpacity>
      </View>

      {/* Barre de recherche et filtres */}
      <View style={styles.searchContainer}>
        <View style={styles.searchBar}>
          <Ionicons name="search" size={20} color="#666" />
          <TextInput
            style={styles.searchInput}
            placeholder="Rechercher un médicament..."
            value={searchQuery}
            onChangeText={setSearchQuery}
          />
        </View>
        <View style={styles.filterContainer}>
          <TouchableOpacity 
            style={[styles.filterButton, activeFilter === 'low' && styles.activeFilter]}
            onPress={() => setActiveFilter(activeFilter === 'low' ? null : 'low')}
          >
            <Text style={[styles.filterText, activeFilter === 'low' && styles.activeFilterText]}>
              Stock faible
            </Text>
          </TouchableOpacity>
          <TouchableOpacity 
            style={[styles.filterButton, activeFilter === 'expiring' && styles.activeFilter]}
            onPress={() => setActiveFilter(activeFilter === 'expiring' ? null : 'expiring')}
          >
            <Text style={[styles.filterText, activeFilter === 'expiring' && styles.activeFilterText]}>
              Périssables
            </Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Liste des médicaments */}
      <ScrollView style={styles.medicationList}>
        {filteredMedications.length === 0 ? (
          <Text style={styles.emptyText}>Aucun médicament trouvé</Text>
        ) : (
          filteredMedications.map(medication => (
            <MedicationCard
              key={medication.id}
              {...medication}
            />
          ))
        )}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
  },
  addButton: {
    padding: 8,
  },
  searchContainer: {
    padding: 16,
  },
  searchBar: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
    borderRadius: 8,
    padding: 8,
    marginBottom: 16,
  },
  searchInput: {
    flex: 1,
    marginLeft: 8,
    fontSize: 16,
  },
  filterContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  filterButton: {
    backgroundColor: '#f0f0f0',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
  },
  activeFilter: {
    backgroundColor: '#007AFF',
  },
  filterText: {
    color: '#666',
  },
  activeFilterText: {
    color: '#fff',
  },
  medicationList: {
    flex: 1,
  },
  emptyText: {
    textAlign: 'center',
    padding: 20,
    color: '#666',
  },
  error: {
    color: 'red',
    textAlign: 'center',
    marginBottom: 16,
  },
  retryButton: {
    backgroundColor: '#007AFF',
    padding: 12,
    borderRadius: 8,
    alignSelf: 'center',
  },
  retryButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
}); 
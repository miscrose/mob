import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
  FlatList,
  ScrollView,
  ActivityIndicator,
} from 'react-native';
import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {API_URL} from '../../../constants/config';
import { useFocusEffect } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';

interface Medication {
  id: number;
  name: string;
  description: string;
  imageUrl: string;
  totalQuantity: number;
  sellPrice: number;
}

interface SaleItem {
  medication: Medication;
  quantity: number;
  total: number;
}

export default function VenteScreen() {
  const [searchText, setSearchText] = useState('');
  const [medications, setMedications] = useState<Medication[]>([]);
  const [filteredMedications, setFilteredMedications] = useState<Medication[]>([]);
  const [selectedMedication, setSelectedMedication] = useState<Medication | null>(null);
  const [quantity, setQuantity] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [saleItems, setSaleItems] = useState<SaleItem[]>([]);

  useFocusEffect(
    React.useCallback(() => {
      loadMedications();
    }, [])
  );

  useEffect(() => {
    loadMedications();
  }, []);

  // Filtrer les médicaments lors de la recherche
  useEffect(() => {
    if (searchText.trim() === '') {
      setFilteredMedications([]);
    } else {
      const filtered = medications.filter(med =>
        med.name.toLowerCase().includes(searchText.toLowerCase())
      );
      setFilteredMedications(filtered);
    }
  }, [searchText, medications]);

  const loadMedications = async () => {
    try {
      const token = await AsyncStorage.getItem('token');
      const pharmacyData = await AsyncStorage.getItem('pharmacyData');
      if (!pharmacyData) throw new Error('Données de la pharmacie non trouvées');
      
      const pharmacy = JSON.parse(pharmacyData);
      
      const response = await axios.get(
        `${API_URL}/api/sales/medications/${pharmacy.id}`,
        {
          headers: { Authorization: `Bearer ${token}` }
        }
      );
      setMedications(response.data);
    } catch (error) {
      console.error('Erreur lors du chargement des médicaments:', error);
      Alert.alert('Erreur', 'Impossible de charger les médicaments');
    }
  };

  const handleAddItem = () => {
    if (!selectedMedication || !quantity) {
      Alert.alert('Erreur', 'Veuillez remplir tous les champs');
      return;
    }

    const quantityNum = parseInt(quantity);
    if (quantityNum > selectedMedication.totalQuantity) {
      Alert.alert('Erreur', 'Quantité supérieure au stock disponible');
      return;
    }

    const newItem: SaleItem = {
      medication: selectedMedication,
      quantity: quantityNum,
      total: quantityNum * selectedMedication.sellPrice
    };

    setSaleItems([...saleItems, newItem]);
    resetForm();
  };

  const resetForm = () => {
    setSelectedMedication(null);
    setSearchText('');
    setQuantity('');
  };

  const removeItem = (index: number) => {
    const newItems = [...saleItems];
    newItems.splice(index, 1);
    setSaleItems(newItems);
  };

  const handleSubmit = async () => {
    if (saleItems.length === 0) {
      Alert.alert('Erreur', 'Veuillez ajouter au moins un médicament');
      return;
    }

    setIsLoading(true);
    try {
      const token = await AsyncStorage.getItem('token');
      const pharmacyData = await AsyncStorage.getItem('pharmacyData');
      if (!pharmacyData) throw new Error('Données de la pharmacie non trouvées');
      
      const pharmacy = JSON.parse(pharmacyData);
      
      // Préparer les données pour l'envoi
      const itemsToSend = saleItems.map(item => ({
        medicationId: item.medication.id,
        quantity: item.quantity
      }));

      await axios.post(
        `${API_URL}/api/sales/create/${pharmacy.id}`,
        itemsToSend,
        {
          headers: { 
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`
          }
        }
      );

      
      await loadMedications();

      Alert.alert('Succès', 'Vente enregistrée avec succès');
      setSaleItems([]);
      resetForm();
    } catch (error: any) {
      console.error('Erreur lors de l\'envoi de la vente:', error);
      const errorMessage = error.response?.data || 'Impossible d\'enregistrer la vente';
      Alert.alert('Erreur', errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const renderSaleItem = ({ item, index }: { item: SaleItem; index: number }) => (
    <View style={styles.saleItem}>
      <View style={styles.saleItemHeader}>
        <Text style={styles.medicationName}>{item.medication.name}</Text>
        <TouchableOpacity
          style={styles.removeButton}
          onPress={() => removeItem(index)}
        >
          <Text style={styles.removeButtonText}>×</Text>
        </TouchableOpacity>
      </View>
      
      <View style={styles.saleItemDetails}>
        <View style={styles.detailRow}>
          <Text style={styles.detailLabel}>Quantité:</Text>
          <Text style={styles.detailValue}>{item.quantity}</Text>
        </View>
        <View style={styles.detailRow}>
          <Text style={styles.detailLabel}>Prix unitaire:</Text>
          <Text style={styles.detailValue}>{item.medication.sellPrice}€</Text>
        </View>
        <View style={styles.detailRow}>
          <Text style={styles.detailLabel}>Total:</Text>
          <Text style={styles.detailValue}>{item.total}€</Text>
        </View>
      </View>
    </View>
  );

  return (
    <View style={styles.container}>
      <View style={styles.formSection}>
        <View style={styles.headerTop}>
          <Text style={styles.title}>Nouvelle vente</Text>
          <TouchableOpacity 
            style={styles.refreshButton}
            onPress={loadMedications}
          >
            <Ionicons name="refresh" size={24} color="#007AFF" />
          </TouchableOpacity>
        </View>
        
        <View style={styles.formContainer}>
          <View style={styles.searchContainer}>
            <TextInput
              style={styles.input}
              placeholder="Rechercher un médicament..."
              value={searchText}
              onChangeText={setSearchText}
            />

            {filteredMedications.length > 0 && (
              <View style={styles.medicationDropdown}>
                {filteredMedications.map((item) => (
                  <TouchableOpacity
                    key={item.id}
                    style={styles.medicationItem}
                    onPress={() => {
                      setSelectedMedication(item);
                      setSearchText(item.name);
                      setFilteredMedications([]);
                    }}
                  >
                    <Text>{item.name}</Text>
                    <Text style={styles.stockInfo}>
                      Stock: {item.totalQuantity} | Prix: {item.sellPrice}€
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            )}
          </View>

          {selectedMedication && (
            <View style={styles.selectedMedicationInfo}>
              <Text style={styles.selectedMedicationName}>
                {selectedMedication.name}
              </Text>
              <Text style={styles.stockInfo}>
                Stock disponible: {selectedMedication.totalQuantity}
              </Text>
              <Text style={styles.priceInfo}>
                Prix unitaire: {selectedMedication.sellPrice}€
              </Text>
              <TextInput
                style={styles.input}
                placeholder="Quantité à vendre"
                value={quantity}
                onChangeText={setQuantity}
                keyboardType="numeric"
              />
              {quantity && (
                <Text style={styles.totalPrice}>
                  Total: {(parseFloat(quantity) * selectedMedication.sellPrice).toFixed(2)}€
                </Text>
              )}
              <TouchableOpacity style={styles.addButton} onPress={handleAddItem}>
                <Text style={styles.buttonText}>Ajouter</Text>
              </TouchableOpacity>
            </View>
          )}
        </View>
      </View>

      {/* Liste des médicaments à vendre */}
      <View style={styles.listSection}>
        {saleItems.length > 0 && (
          <FlatList
            data={saleItems}
            keyExtractor={(item, index) => index.toString()}
            renderItem={renderSaleItem}
            style={styles.saleList}
          />
        )}
      </View>

      {/* Bouton de soumission */}
      {saleItems.length > 0 && (
        <View style={styles.submitSection}>
          <TouchableOpacity
            style={[styles.submitButton, isLoading && styles.submitButtonDisabled]}
            onPress={handleSubmit}
            disabled={isLoading}
          >
            <Text style={styles.buttonText}>
              {isLoading ? 'Envoi en cours...' : 'Valider la vente'}
            </Text>
          </TouchableOpacity>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  formSection: {
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  formContainer: {
    marginBottom: 10,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 20,
  },
  input: {
    backgroundColor: '#f5f5f5',
    padding: 15,
    borderRadius: 10,
    marginBottom: 15,
    fontSize: 16,
  },
  searchContainer: {
    position: 'relative',
    zIndex: 1,
  },
  medicationDropdown: {
    position: 'absolute',
    top: '100%',
    left: 0,
    right: 0,
    backgroundColor: 'white',
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 5,
    maxHeight: 200,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
  },
  medicationItem: {
    padding: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  medicationHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  medicationName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  stockQuantity: {
    fontSize: 14,
    fontWeight: '500',
  },
  inStock: {
    color: '#34C759',
  },
  outOfStock: {
    color: '#FF3B30',
  },
  lotsContainer: {
    marginTop: 5,
  },
  lotItem: {
    padding: 5,
  },
  lotText: {
    fontSize: 12,
    color: '#666',
  },
  selectedMedicationInfo: {
    marginTop: 10,
    padding: 10,
    backgroundColor: '#f9f9f9',
    borderRadius: 10,
  },
  selectedMedicationName: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 5,
  },
  stockInfo: {
    fontSize: 14,
    color: '#666',
    marginBottom: 10,
  },
  priceInfo: {
    fontSize: 16,
    color: '#007AFF',
    fontWeight: '500',
    marginBottom: 10,
  },
  totalPrice: {
    fontSize: 18,
    color: '#34C759',
    fontWeight: 'bold',
    marginTop: 10,
    textAlign: 'right',
  },
  listSection: {
    flex: 1,
    backgroundColor: '#f9f9f9',
  },
  submitSection: {
    padding: 16,
    borderTopWidth: 1,
    borderTopColor: '#eee',
    backgroundColor: '#fff',
  },
  saleList: {
    flex: 1,
  },
  saleItem: {
    backgroundColor: '#fff',
    borderRadius: 10,
    margin: 8,
    padding: 10,
    borderWidth: 1,
    borderColor: '#eee',
  },
  saleItemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
    paddingBottom: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  saleItemDetails: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  detailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    width: '48%',
    marginBottom: 4,
  },
  detailLabel: {
    fontSize: 14,
    color: '#666',
    marginRight: 5,
  },
  detailValue: {
    fontSize: 14,
    fontWeight: '500',
    color: '#333',
  },
  removeButton: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#FF3B30',
    justifyContent: 'center',
    alignItems: 'center',
  },
  removeButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  submitButton: {
    backgroundColor: '#34C759',
    padding: 15,
    borderRadius: 10,
    alignItems: 'center',
  },
  submitButtonDisabled: {
    backgroundColor: '#ccc',
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  addButton: {
    backgroundColor: '#34C759',
    padding: 15,
    borderRadius: 10,
    alignItems: 'center',
    marginTop: 10,
  },
  headerTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  refreshButton: {
    padding: 8,
    borderRadius: 8,
    backgroundColor: '#f0f0f0',
  },
}); 
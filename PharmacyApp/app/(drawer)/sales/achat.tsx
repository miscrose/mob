import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
  Alert,
  FlatList,
  Platform,
  Modal,
} from 'react-native';

import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {API_URL} from '../../../constants/config';



interface Medication {
  id: number;
  name: string;
  description: string;
  imageUrl: string;
}

interface PurchaseItem {
  medication: Medication;
  quantity: number;
  unitPrice: number;
  expirationDate: Date;
  total: number;
}

export default function AchatScreen() {
  const [searchText, setSearchText] = useState('');
  const [medications, setMedications] = useState<Medication[]>([]);
  const [filteredMedications, setFilteredMedications] = useState<Medication[]>([]);
  const [selectedMedication, setSelectedMedication] = useState<Medication | null>(null);
  const [quantity, setQuantity] = useState('');
  const [unitPrice, setUnitPrice] = useState('');
  const [expirationDate, setExpirationDate] = useState(new Date());
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [purchaseItems, setPurchaseItems] = useState<PurchaseItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedDate, setSelectedDate] = useState(new Date());

  const months = [
    'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
    'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'
  ];


  const getDaysInMonth = (month: number, year: number) => {
    return new Date(year, month + 1, 0).getDate();
  };

  const handleDateSelect = (day: number, month: number, year: number) => {
    const newDate = new Date(year, month, day);
    setSelectedDate(newDate);
    setExpirationDate(newDate);
    setShowDatePicker(false);
  };

  const renderCalendar = () => {
    const currentMonth = selectedDate.getMonth();
    const currentYear = selectedDate.getFullYear();
    const daysInMonth = getDaysInMonth(currentMonth, currentYear);
    const firstDayOfMonth = new Date(currentYear, currentMonth, 1).getDay();
    
    const daysArray = Array.from({ length: 42 }, (_, i) => {
      const dayNumber = i - firstDayOfMonth + 1;
      return dayNumber > 0 && dayNumber <= daysInMonth ? dayNumber : null;
    });

    return (
      <View style={styles.calendarContainer}>
        <View style={styles.calendarHeader}>
          <TouchableOpacity
            style={styles.navButton}
            onPress={() => {
              const newDate = new Date(selectedDate);
              newDate.setMonth(newDate.getMonth() - 1);
              setSelectedDate(newDate);
            }}
          >
            <Text style={styles.navButtonText}>←</Text>
          </TouchableOpacity>
          
          <Text style={styles.monthYearText}>
            {months[currentMonth]} {currentYear}
          </Text>
          
          <TouchableOpacity
            style={styles.navButton}
            onPress={() => {
              const newDate = new Date(selectedDate);
              newDate.setMonth(newDate.getMonth() + 1);
              setSelectedDate(newDate);
            }}
          >
            <Text style={styles.navButtonText}>→</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.weekDaysContainer}>
          {['Dim', 'Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam'].map((day) => (
            <Text key={day} style={styles.weekDayText}>{day}</Text>
          ))}
        </View>

        <View style={styles.daysGrid}>
          {daysArray.map((day, index) => (
            <TouchableOpacity
              key={index}
              style={[
                styles.dayButton,
                day === selectedDate.getDate() && styles.selectedDay,
                !day && styles.emptyDay
              ]}
              onPress={() => day && handleDateSelect(day, currentMonth, currentYear)}
              disabled={!day}
            >
              <Text style={[
                styles.dayText,
                day === selectedDate.getDate() && styles.selectedDayText
              ]}>
                {day}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>
    );
  };

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

  useEffect(() => {
    console.log('showDatePicker changed:', showDatePicker);
  }, [showDatePicker]);

  const loadMedications = async () => {
    try {
      const token = await AsyncStorage.getItem('token');
      const response = await axios.get(`${API_URL}/api/medications/allmedications`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setMedications(response.data);
    } catch (error) {
      console.error('Erreur lors du chargement des médicaments:', error);
      Alert.alert('Erreur', 'Impossible de charger les médicaments');
    }
  };

  const handleAddItem = () => {
    if (!selectedMedication || !quantity || !unitPrice) {
      Alert.alert('Erreur', 'Veuillez remplir tous les champs');
      return;
    }

    const newItem: PurchaseItem = {
      medication: selectedMedication,
      quantity: parseInt(quantity),
      unitPrice: parseFloat(unitPrice),
      expirationDate,
      total: parseInt(quantity) * parseFloat(unitPrice)
    };

    setPurchaseItems([...purchaseItems, newItem]);
    resetForm();
  };

  const resetForm = () => {
    setSelectedMedication(null);
    setSearchText('');
    setQuantity('');
    setUnitPrice('');
    setExpirationDate(new Date());
  };

  const handleSubmit = async () => {
    if (purchaseItems.length === 0) {
      Alert.alert('Erreur', 'Veuillez ajouter au moins un médicament');
      return;
    }

    setIsLoading(true);
    try {
      const token = await AsyncStorage.getItem('token');
      const pharmacyData = await AsyncStorage.getItem('pharmacyData');
      if (!pharmacyData) throw new Error('Données de la pharmacie non trouvées');
      
      const pharmacy = JSON.parse(pharmacyData);
      
      await axios.post(
        `${API_URL}/api/purchases/createPurchase`,
      
        {
          pharmacyId: pharmacy.id,
          items: purchaseItems.map(item => ({
            medicationId: item.medication.id,
            quantity: item.quantity,
            unitPrice: item.unitPrice,
            expirationDate: item.expirationDate.toISOString()
          }))
        },
        {
          headers: { Authorization: `Bearer ${token}` }
        }
      );

      Alert.alert('Succès', 'Achats enregistrés avec succès');
      setPurchaseItems([]);
      resetForm();
    } catch (error) {
      console.error('Erreur lors de l\'envoi des achats:', error);
      Alert.alert('Erreur', 'Impossible d\'enregistrer les achats');
    } finally {
      setIsLoading(false);
    }
  };

  const removeItem = (index: number) => {
    const newItems = [...purchaseItems];
    newItems.splice(index, 1);
    setPurchaseItems(newItems);
  };

  const renderPurchaseItem = ({ item, index }: { item: PurchaseItem; index: number }) => (
    <View style={styles.purchaseItem}>
      <View style={styles.purchaseItemHeader}>
        <Text style={styles.medicationName}>{item.medication.name}</Text>
        <TouchableOpacity
          style={styles.removeButton}
          onPress={() => removeItem(index)}
        >
          <Text style={styles.removeButtonText}>×</Text>
        </TouchableOpacity>
      </View>
      
      <View style={styles.purchaseItemDetails}>
        <View style={styles.detailRow}>
          <Text style={styles.detailLabel}>Quantité:</Text>
          <Text style={styles.detailValue}>{item.quantity}</Text>
        </View>
        <View style={styles.detailRow}>
          <Text style={styles.detailLabel}>Prix unitaire:</Text>
          <Text style={styles.detailValue}>{item.unitPrice}€</Text>
        </View>
        <View style={styles.detailRow}>
          <Text style={styles.detailLabel}>Total:</Text>
          <Text style={styles.detailValue}>{item.total}€</Text>
        </View>
        <View style={styles.detailRow}>
          <Text style={styles.detailLabel}>Expiration:</Text>
          <Text style={styles.detailValue}>{item.expirationDate.toLocaleDateString()}</Text>
        </View>
      </View>
    </View>
  );

  return (
    <View style={styles.container}>
   
      <View style={styles.formSection}>
        
        
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
                  </TouchableOpacity>
                ))}
              </View>
            )}
          </View>

          <View style={styles.inputRow}>
            <TextInput
              style={[styles.input, styles.smallInput]}
              placeholder="Quantité"
              value={quantity}
              onChangeText={setQuantity}
              keyboardType="numeric"
            />
            <TextInput
              style={[styles.input, styles.smallInput]}
              placeholder="Prix unitaire"
              value={unitPrice}
              onChangeText={setUnitPrice}
              keyboardType="numeric"
            />
          </View>

          <View style={styles.dateContainer}>
            <TouchableOpacity
              style={styles.dateButton}
              onPress={() => setShowDatePicker(true)}
            >
              <Text style={styles.dateButtonText}>
                {selectedDate.toLocaleDateString('fr-FR', {
                  day: '2-digit',
                  month: 'long',
                  year: 'numeric'
                })}
              </Text>
            </TouchableOpacity>
          </View>

          <TouchableOpacity style={styles.addButton} onPress={handleAddItem}>
            <Text style={styles.buttonText}>Ajouter</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Liste des achats avec scroll */}
      <View style={styles.listSection}>
        {purchaseItems.length > 0 && (
          <FlatList
            data={purchaseItems}
            keyExtractor={(item, index) => index.toString()}
            renderItem={renderPurchaseItem}
            style={styles.purchaseList}
          />
        )}
      </View>

      {/* Bouton de soumission fixé en bas */}
      {purchaseItems.length > 0 && (
        <View style={styles.submitSection}>
          <TouchableOpacity
            style={[styles.submitButton, isLoading && styles.submitButtonDisabled]}
            onPress={handleSubmit}
            disabled={isLoading}
          >
            <Text style={styles.buttonText}>
              {isLoading ? 'Envoi en cours...' : 'Valider les achats'}
            </Text>
          </TouchableOpacity>
        </View>
      )}

      {/* Modal du calendrier */}
      {showDatePicker && (
        <Modal
          visible={showDatePicker}
          transparent={true}
          animationType="slide"
        >
          <View style={styles.modalContainer}>
            <View style={styles.modalContent}>
              <Text style={styles.modalTitle}>Sélectionner une date</Text>
              {renderCalendar()}
              <TouchableOpacity
                style={styles.modalButton}
                onPress={() => setShowDatePicker(false)}
              >
                <Text style={styles.modalButtonText}>Fermer</Text>
              </TouchableOpacity>
            </View>
          </View>
        </Modal>
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
  formContainer: {
    marginBottom: 10,
  },
  inputRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 10,
  },
  smallInput: {
    flex: 1,
  },
  purchaseList: {
    flex: 1,
  },
  purchaseItem: {
    backgroundColor: '#fff',
    borderRadius: 10,
    margin: 8,
    padding: 10,
    borderWidth: 1,
    borderColor: '#eee',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 20,
  },
  subtitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginVertical: 15,
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
  dateContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 10,
  },
  dateButton: {
    flex: 1,
    marginHorizontal: 5,
    padding: 10,
    backgroundColor: '#f5f5f5',
    borderRadius: 5,
    alignItems: 'center',
  },
  dateButtonText: {
    fontSize: 16,
  },
  selectedDateText: {
    marginTop: 10,
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
  },
  addButton: {
    backgroundColor: '#007AFF',
    padding: 15,
    borderRadius: 10,
    alignItems: 'center',
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  purchaseItemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
    paddingBottom: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  medicationName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  purchaseItemDetails: {
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
    marginTop: 20,
    alignItems: 'center',
  },
  submitButtonDisabled: {
    backgroundColor: '#ccc',
  },
  modalContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  modalContent: {
    backgroundColor: 'white',
    borderRadius: 10,
    padding: 20,
    width: '90%',
    maxHeight: '80%',
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  calendarContainer: {
    width: '100%',
    backgroundColor: 'white',
    borderRadius: 10,
    padding: 10,
  },
  calendarHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 15,
  },
  navButton: {
    padding: 10,
  },
  navButtonText: {
    fontSize: 20,
    color: '#007AFF',
  },
  monthYearText: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  weekDaysContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 10,
  },
  weekDayText: {
    width: 40,
    textAlign: 'center',
    color: '#666',
    fontWeight: 'bold',
  },
  daysGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'flex-start',
  },
  dayButton: {
    width: 40,
    height: 40,
    justifyContent: 'center',
    alignItems: 'center',
    margin: 2,
    borderRadius: 20,
  },
  selectedDay: {
    backgroundColor: '#007AFF',
  },
  emptyDay: {
    opacity: 0,
  },
  dayText: {
    fontSize: 16,
  },
  selectedDayText: {
    color: 'white',
  },
  modalButton: {
    marginTop: 20,
    padding: 15,
    backgroundColor: '#007AFF',
    borderRadius: 5,
    alignItems: 'center',
  },
  modalButtonText: {
    color: 'white',
    fontWeight: 'bold',
  },
}); 
import React, { useState } from 'react';
import { View, Text, StyleSheet, Image, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { MedicationStock, StockLot } from '../../constants/types/stock';
import { API_URL } from '../../constants/config';

export default function MedicationCard({ id, name, imageUrl, totalQuantity, lots }: MedicationStock) {
  const [imageError, setImageError] = useState(false);
  const [expanded, setExpanded] = useState(false); // 👈 État pour savoir si c'est déplié
  const fullImageUrl = imageUrl ? `${API_URL}/${imageUrl}` : null;

  return (
    <TouchableOpacity onPress={() => setExpanded(!expanded)} style={styles.card}>
      <View style={styles.imageContainer}>
        {!imageError && fullImageUrl ? (
          <Image 
            source={{ uri: fullImageUrl }} 
            style={styles.image}
            onError={() => setImageError(true)}
          />
        ) : (
          <View style={[styles.image, styles.placeholderImage]}>
            <Ionicons name="medkit-outline" size={30} color="#666" />
          </View>
        )}
      </View>
      <View style={styles.content}>
        <View style={styles.header}>
          <Text style={styles.name}>{name}</Text>
          <TouchableOpacity onPress={() => setExpanded(!expanded)}>
            <Ionicons 
              name={expanded ? 'chevron-up' : 'chevron-down'} 
              size={24} 
              color="#666" 
            />
          </TouchableOpacity>
        </View>
        <Text style={styles.quantity}>Quantité totale: {totalQuantity}</Text>

        {expanded && ( // 👈 On affiche les lots seulement si c'est "expanded"
          <View style={styles.lotsContainer}>
            {lots.map((lot: StockLot) => (
              <View key={lot.id} style={styles.lot}>
                <Text style={styles.lotQuantity}>{lot.quantity} unités</Text>
                <Text style={styles.expirationDate}>
                  Expire le: {new Date(lot.expirationDate).toLocaleDateString()}
                </Text>
              </View>
            ))}
          </View>
        )}
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
    alignItems: 'flex-start',
  },
  imageContainer: {
    width: 60,
    height: 60,
    marginRight: 16,
  },
  image: {
    width: 60,
    height: 60,
    borderRadius: 8,
  },
  placeholderImage: {
    backgroundColor: '#f0f0f0',
    justifyContent: 'center',
    alignItems: 'center',
  },
  content: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  name: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  quantity: {
    fontSize: 14,
    color: '#666',
    marginBottom: 8,
  },
  lotsContainer: {
    marginTop: 8,
  },
  lot: {
    backgroundColor: '#f5f5f5',
    padding: 8,
    borderRadius: 4,
    marginBottom: 4,
  },
  lotQuantity: {
    fontSize: 12,
    color: '#333',
  },
  expirationDate: {
    fontSize: 12,
    color: '#666',
  },
});

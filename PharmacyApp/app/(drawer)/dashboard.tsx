import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, Dimensions, TouchableOpacity, ActivityIndicator } from 'react-native';
import { LineChart, BarChart } from 'react-native-chart-kit';
import { API_URL } from '../../constants/config';
import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Picker } from '@react-native-picker/picker';
import { Ionicons } from '@expo/vector-icons';
import { useFocusEffect } from '@react-navigation/native';

interface DailySales {
  date: string;
  numberOfSales: number;
  revenue: number;
  topSellingMedications: { [key: string]: number };
}

interface DashboardData {
  dailySales: DailySales[];
  totalRevenue: number;
  totalSales: number;
}

export default function Dashboard() {
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedPeriod, setSelectedPeriod] = useState('30');

  const periods = [
    { label: '7 derniers jours', value: '7' },
    { label: '15 derniers jours', value: '15' },
    { label: '30 derniers jours', value: '30' },
    { label: '60 derniers jours', value: '60' },
    { label: '90 derniers jours', value: '90' }
  ];

  useFocusEffect(
    React.useCallback(() => {
    fetchDashboardData();
    }, [selectedPeriod])
  );

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const token = await AsyncStorage.getItem('token');
      if (!token) {
        throw new Error('Token non trouvé');
      }

      const pharmacyData = await AsyncStorage.getItem('pharmacyData');
      if (!pharmacyData) {
        throw new Error('Données de la pharmacie non trouvées');
      }

      const pharmacy = JSON.parse(pharmacyData);
      const pharmacyId = pharmacy.id;

      const response = await axios.get(
        `${API_URL}/api/dashboard/sales/stats?pharmacyId=${pharmacyId}&days=${selectedPeriod}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );
      setDashboardData(response.data);
      console.log(response.data);
    } catch (error) {
      console.error('Erreur lors de la récupération des données:', error);
      if (axios.isAxiosError(error)) {
        console.error('Réponse d\'erreur:', error.response?.data);
      }
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#007AFF" />
        <Text style={styles.loadingText}>Chargement des données...</Text>
      </View>
    );
  }

  if (!dashboardData) {
    return (
      <View style={styles.errorContainer}>
        <Ionicons name="alert-circle-outline" size={50} color="#FF3B30" />
        <Text style={styles.errorText}>Erreur lors du chargement des données</Text>
      </View>
    );
  }

  if (dashboardData.dailySales.length === 0) {
    return (
      <View style={styles.emptyContainer}>
        <Ionicons name="stats-chart-outline" size={50} color="#007AFF" />
        <Text style={styles.emptyText}>Aucune donnée disponible pour la période sélectionnée</Text>
        <Text style={styles.emptySubText}>Les statistiques apparaîtront dès que des ventes seront enregistrées</Text>
      </View>
    );
  }

  const salesChartConfig = {
    backgroundGradientFrom: "#ffffff",
    backgroundGradientTo: "#ffffff",
    color: (opacity = 1) => `rgba(0, 122, 255, ${opacity})`,
    strokeWidth: 2,
    barPercentage: 0.5,
    useShadowColorFromDataset: false,
    formatYLabel: (yLabel: string) => {
      const value = parseFloat(yLabel);
      if (isNaN(value)) return yLabel;
      if (value === 0) return '0';
      return value.toFixed(0);
    }
  };

  const revenueChartConfig = {
    backgroundGradientFrom: "#ffffff",
    backgroundGradientTo: "#ffffff",
    color: (opacity = 1) => `rgba(0, 122, 255, ${opacity})`,
    strokeWidth: 2,
    barPercentage: 0.5,
    useShadowColorFromDataset: false,
    formatYLabel: (yLabel: string) => {
      const value = parseFloat(yLabel);
      if (isNaN(value)) return yLabel;
      return value.toFixed(2);
    }
  };

  const screenWidth = Dimensions.get("window").width;

  const salesData = {
    labels: dashboardData.dailySales.map(sale => {
      const [year, month, day] = sale.date.split('-');
      return `${day}/${month}`;
    }),
    datasets: [{
      data: dashboardData.dailySales.map(sale => sale.numberOfSales)
    }]
  };

  const revenueData = {
    labels: dashboardData.dailySales.map(sale => {
      const [year, month, day] = sale.date.split('-');
      return `${day}/${month}`;
    }),
    datasets: [{
      data: dashboardData.dailySales.map(sale => sale.revenue)
    }]
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <View style={styles.headerTop}>
          <Text style={styles.title}>Tableau de bord</Text>
          <TouchableOpacity 
            style={styles.refreshButton}
            onPress={fetchDashboardData}
          >
            <Ionicons name="refresh" size={24} color="#007AFF" />
          </TouchableOpacity>
        </View>
        <View style={styles.periodSelector}>
          <Text style={styles.periodLabel}>Période :</Text>
          <View style={styles.pickerContainer}>
            <Picker
              selectedValue={selectedPeriod}
              onValueChange={(value) => setSelectedPeriod(value)}
              style={styles.picker}
              dropdownIconColor="#007AFF"
            >
              {periods.map((period) => (
                <Picker.Item 
                  key={period.value} 
                  label={period.label} 
                  value={period.value}
                  color="#333"
                />
              ))}
            </Picker>
          </View>
        </View>
      </View>

      <View style={styles.statsContainer}>
        <View style={styles.statBox}>
          <Ionicons name="cart-outline" size={24} color="#007AFF" />
          <Text style={styles.statTitle}>Ventes Totales</Text>
          <Text style={styles.statValue}>{dashboardData.totalSales}</Text>
        </View>
        <View style={styles.statBox}>
          <Ionicons name="cash-outline" size={24} color="#34C759" />
          <Text style={styles.statTitle}>Revenu Total</Text>
          <Text style={styles.statValue}>{dashboardData.totalRevenue.toFixed(2)} €</Text>
        </View>
      </View>

      <View style={styles.chartContainer}>
        <Text style={styles.sectionTitle}>Ventes quotidiennes</Text>
        <LineChart
          data={salesData}
          width={screenWidth - 60}
          height={220}
          chartConfig={salesChartConfig}
          bezier
          style={styles.chart}
          yAxisLabel=""
          yAxisSuffix=""
          yAxisInterval={1}
          formatYLabel={(yLabel) => parseInt(yLabel).toString()} 
        />
      </View>

      <View style={styles.chartContainer}>
        <Text style={styles.sectionTitle}>Revenus quotidiens</Text>
        <BarChart
          data={revenueData}
          width={screenWidth - 60}
          height={220}
          chartConfig={revenueChartConfig}
          style={styles.chart}
          yAxisLabel="€"
          yAxisSuffix=""
          yAxisInterval={1}
        />
      </View>

      <View style={styles.medicationsContainer}>
        <Text style={styles.sectionTitle}>Médicaments les plus vendus</Text>
        {dashboardData.dailySales.slice().reverse().map((day, index) => (
          <View key={index} style={styles.medicationContainer}>
            <Text style={styles.dateTitle}>{day.date}</Text>
            {Object.entries(day.topSellingMedications).map(([name, quantity]) => (
              <View key={name} style={styles.medicationRow}>
                <Text style={styles.medicationName}>{name}</Text>
                <Text style={styles.medicationQuantity}>{quantity} unités</Text>
              </View>
            ))}
          </View>
        ))}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  loadingText: {
    marginTop: 10,
    fontSize: 16,
    color: '#666',
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  errorText: {
    marginTop: 10,
    fontSize: 16,
    color: '#FF3B30',
  },
  header: {
    padding: 20,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 20,
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
  periodSelector: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f8f9fa',
    padding: 15,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#e9ecef',
    marginHorizontal: 10,
    minHeight: 60,
  },
  periodLabel: {
    fontSize: 15,
    fontWeight: '600',
    color: '#333',
    marginRight: 10,
    minWidth: 60,
  },
  pickerContainer: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#007AFF',
    minHeight: 50,
    justifyContent: 'center',
    paddingHorizontal: 10,
    minWidth: 250,
  },
  picker: {
    height: 50,
    color: '#333',
    fontSize: 14,
    width: '100%',
  },
  statsContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    padding: 20,
  },
  statBox: {
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 12,
    width: '48%',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
    alignItems: 'center',
  },
  statTitle: {
    fontSize: 14,
    color: '#666',
    marginTop: 8,
    marginBottom: 4,
  },
  statValue: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
  },
  chartContainer: {
    backgroundColor: '#fff',
    margin: 20,
    padding: 15,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 15,
  },
  chart: {
    marginVertical: 8,
    borderRadius: 16,
  },
  medicationsContainer: {
    padding: 20,
  },
  medicationContainer: {
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 12,
    marginBottom: 15,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  dateTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
    paddingBottom: 8,
  },
  medicationRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#f5f5f5',
  },
  medicationName: {
    fontSize: 15,
    color: '#333',
  },
  medicationQuantity: {
    fontSize: 15,
    fontWeight: '600',
    color: '#007AFF',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
    padding: 20,
  },
  emptyText: {
    marginTop: 15,
    fontSize: 18,
    fontWeight: '600',
    color: '#333',
    textAlign: 'center',
  },
  emptySubText: {
    marginTop: 8,
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
  },
}); 
import { Drawer } from 'expo-router/drawer';
import { FontAwesome } from '@expo/vector-icons';
import { View, TouchableOpacity, Text, StyleSheet } from 'react-native';
import { useRouter } from 'expo-router';

export default function DrawerLayout() {
  const router = useRouter();

  const handleLogout = () => {
    router.replace('/login');
  };

  return (
    <Drawer
      screenOptions={{
        headerStyle: {
          backgroundColor: '#f4511e',
        },
        headerTintColor: '#fff',
      }}
    >
      <Drawer.Screen 
        name="index" 
        options={{ 
          title: 'Accueil',
          drawerIcon: ({ color, size }: { color: string; size: number }) => (
            <FontAwesome name="home" size={size} color={color} />
          ),
        }} 
      />
      <Drawer.Screen 
        name="dashboard" 
        options={{ 
          title: 'Tableau de bord',
          drawerIcon: ({ color, size }: { color: string; size: number }) => (
            <FontAwesome name="dashboard" size={size} color={color} />
          ),
        }} 
      />
      <Drawer.Screen 
        name="stock" 
        options={{ 
          title: 'Gestion du Stock',
          drawerIcon: ({ color, size }: { color: string; size: number }) => (
            <FontAwesome name="cube" size={size} color={color} />
          ),
        }} 
      />
      <Drawer.Screen 
        name="sales" 
        options={{ 
          title: 'Vente/Achat',
          drawerIcon: ({ color, size }: { color: string; size: number }) => (
            <FontAwesome name="exchange" size={size} color={color} />
          ),
        }} 
      />
      <Drawer.Screen 
        name="add-medication" 
        options={{ 
          title: 'Ajouter Médicament',
          drawerIcon: ({ color, size }: { color: string; size: number }) => (
            <FontAwesome name="plus-circle" size={size} color={color} />
          ),
        }} 
      />
      <Drawer.Screen 
        name="logout" 
        options={{ 
          title: 'Déconnexion',
          drawerIcon: ({ color, size }: { color: string; size: number }) => (
            <FontAwesome name="sign-out" size={size} color={color} />
          ),
        }}
        listeners={{
          focus: () => {
            handleLogout();
          }
        }}
      />
    </Drawer>
  );
}
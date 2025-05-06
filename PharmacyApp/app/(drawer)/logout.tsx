import { Redirect } from 'expo-router';
import { useEffect, useState } from 'react';
import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { getExpoPushTokenAsync } from 'expo-notifications';
import { API_URL } from '../../constants/config';
 
export default function Logout() {
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    console.log('Début du processus de déconnexion');
    
    const deleteToken = async () => {
      try {
        console.log('Récupération du token...');
        const token = await getExpoPushTokenAsync();
        console.log('Token récupéré:', token.data);
        
        const jwtToken = await AsyncStorage.getItem('token');
        const encodedToken = encodeURIComponent(token.data);
        console.log('Token à supprimer:', token.data);
        console.log('Token encodé:', encodedToken);
        
        console.log('Envoi de la requête de suppression...');
        await axios.delete(`${API_URL}/api/device-tokens/${encodedToken}`, {
          headers: { 
            Authorization: `Bearer ${jwtToken}`
          }
        });
        console.log('Token supprimé avec succès');
        
        // Attendre 2 secondes pour s'assurer que les logs sont affichés
        await new Promise(resolve => setTimeout(resolve, 2000));
      } catch (error) {
        console.error('Erreur lors de la suppression du token:', error);
        // Attendre 2 secondes même en cas d'erreur
        await new Promise(resolve => setTimeout(resolve, 2000));
      } finally {
        console.log('Fin du processus de déconnexion');
        setIsReady(true);
      }
    };

    deleteToken();
  }, []);

  if (!isReady) {
    return null;
  }

  return <Redirect href="/login" />;
} 
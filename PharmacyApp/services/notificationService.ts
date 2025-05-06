import { registerForPushNotificationsAsync } from '../constants/notifications';
import axios from 'axios';
import { API_URL } from '../constants/config';

export const registerDeviceToken = async (pharmacyId: string, jwtToken: string) => {
  try {
    const token = await registerForPushNotificationsAsync();
    
    if (!token) {
      throw new Error("Impossible d'obtenir le token de notification");
    }

    const response = await axios.post(`${API_URL}/api/device-tokens`, null, {
      params: {
        token: encodeURIComponent(token),
        pharmacyId: Number(pharmacyId)
      },
      headers: {
        'Authorization': `Bearer ${jwtToken}`
      }
    });

    console.log('Réponse du serveur:', response.data);
    return response.data;
  } catch (error) {
   
    return null;
  }
}; 
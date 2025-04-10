import { Tabs } from 'expo-router';
import { FontAwesome } from '@expo/vector-icons';

export default function SalesLayout() {
  return (
    <Tabs>

      <Tabs.Screen
        name="achat"
        options={{
          title: 'Achat',
          tabBarIcon: ({ color, size }: { color: string; size: number }) => (
            <FontAwesome name="truck" size={size} color={color} />
          ),
        }}
      />
      <Tabs.Screen
        name="vente"
        options={{
          title: 'Vente',
          tabBarIcon: ({ color, size }: { color: string; size: number }) => (
            <FontAwesome name="shopping-cart" size={size} color={color} />
          ),
        }}
      />
      
    </Tabs>
  );
} 
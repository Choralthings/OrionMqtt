- Vers 1.23 [27/06/2014] [MB]

	- Corretto bug su messaggio SMS richiesto da "gps"

- Vers 1.22 [26/06/2014] [MB]
	
	- Aggiunto @LOG su protocollo seriale
	- Modificato task GPS
	- Tolto reboot per blocco gps
	
- Vers 1.21 [12/06/2014] [MB]

	- Aggiunta la variabile "mqttParser" per indicare in che modo i dati vengono spediti

- Vers 1.20 [28/05/2014] [MB]

	- Cambiato nome del file di impostazioni in "GWmqttSettings.txt" ed al suo interno modificato il titolo
	  in Greenwich Settings. Modificato il parametro SoftLog con PublishTopic
	- Modificati tutti i parametri che riportano al SoftLog
	- Modificato il file SocketGPRStask.java per catturare i dati della connessione MQTT dal file di 
	  impostazioni.
	- Eliminate System.out di debug
	- Eliminati allarmi di variazione di inputs
	
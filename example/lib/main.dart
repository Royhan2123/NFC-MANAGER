import 'package:flutter/material.dart';
import 'nfc_pro_api.dart';

void main() {
  runApp(const NfcProExampleApp());
}

class NfcProExampleApp extends StatelessWidget {
  const NfcProExampleApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData.dark().copyWith(
        scaffoldBackgroundColor: const Color(0xFF000000),
        primaryColor: const Color(0xFF0A84FF),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF0A84FF),
          secondary: Color(0xFFBF5AF2),
        ),
      ),
      home: const DashboardScreen(),
    );
  }
}

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  String _status = "Ready to Scan";
  String _tagData = "No tag detected yet.";
  bool _isScanning = false;

  @override
  void initState() {
    super.initState();
    _initNfcListener();
  }

  void _initNfcListener() {
    NfcPro.onTagDiscovered.listen((data) {
      setState(() {
        _status = "Tag Detected!";
        _tagData =
            "UID: ${data['uid']}\nType: ${data['type']}\nContent: ${data['content'] ?? 'Empty'}";
      });
    }).onError((error) {
      setState(() {
        _status = "Error: $error";
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("NFC Pro Command Center"),
        elevation: 0,
        backgroundColor: Colors.transparent,
      ),
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              "NFC STATUS",
              style: TextStyle(
                  color: Colors.grey,
                  fontSize: 12,
                  fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 10),
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: const Color(0xFF1C1C1E),
                borderRadius: BorderRadius.circular(15),
              ),
              child: Row(
                children: [
                  Icon(
                    _isScanning ? Icons.sync : Icons.nfc,
                    color: _isScanning ? Colors.blue : Colors.grey,
                    size: 30,
                  ),
                  const SizedBox(width: 15),
                  Text(
                    _status,
                    style: const TextStyle(
                        fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 30),
            const Text(
              "LAST SCAN DATA",
              style: TextStyle(
                  color: Colors.grey,
                  fontSize: 12,
                  fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 10),
            Expanded(
              child: Container(
                width: double.infinity,
                padding: const EdgeInsets.all(15),
                decoration: BoxDecoration(
                  color: const Color(0xFF1C1C1E),
                  borderRadius: BorderRadius.circular(15),
                ),
                child: SingleChildScrollView(
                  child: Text(
                    _tagData,
                    style:
                        const TextStyle(fontFamily: 'monospace', fontSize: 14),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 20),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    onPressed: () async {
                      bool success = await NfcPro.startScan();
                      setState(() => _isScanning = success);
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF0A84FF),
                      padding: const EdgeInsets.symmetric(vertical: 15),
                    ),
                    child: const Text("Start Scan"),
                  ),
                ),
                const SizedBox(width: 15),
                Expanded(
                  child: ElevatedButton(
                    onPressed: () async {
                      await NfcPro.stopScan();
                      setState(() => _isScanning = false);
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.redAccent,
                      padding: const EdgeInsets.symmetric(vertical: 15),
                    ),
                    child: const Text("Stop Scan"),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 15),
            SizedBox(
              width: double.infinity,
              child: OutlinedButton(
                onPressed: () {
                  // Show cloning dialog
                  _showCloneDialog();
                },
                child: const Text("Identity Cloning"),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _showCloneDialog() {
    TextEditingController controller = TextEditingController();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Clone Identity"),
        content: TextField(
          controller: controller,
          decoration: const InputDecoration(hintText: "Enter ID to clone"),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text("Cancel"),
          ),
          TextButton(
            onPressed: () async {
              await NfcPro.setClonedId(controller.text);
              if (mounted) Navigator.pop(context);
            },
            child: const Text("Clone"),
          ),
        ],
      ),
    );
  }
}

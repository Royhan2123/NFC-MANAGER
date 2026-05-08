import 'package:flutter/material.dart';
import 'package:nfc_pro_manager/nfc_pro_manager.dart';

void main() {
  runApp(const NfcProExampleApp());
}

class NfcProExampleApp extends StatelessWidget {
  const NfcProExampleApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorSchemeSeed: Colors.deepPurple,
        brightness: Brightness.dark,
      ),
      home: const NfcDashboard(),
    );
  }
}

class NfcDashboard extends StatefulWidget {
  const NfcDashboard({super.key});

  @override
  State<NfcDashboard> createState() => _NfcDashboardState();
}

class _NfcDashboardState extends State<NfcDashboard> {
  String _status = "Ready to Scan";
  String _tagData = "No data yet";
  bool _isNfcAvailable = false;
  bool _isHceSupported = false;
  String _mifareStatus = "No MIFARE action yet";
  List<String>? _mifareBlocks;
  String _mifareKeyA = 'FFFFFFFFFFFF';
  final TextEditingController _mifareKeyController =
      TextEditingController(text: 'FFFFFFFFFFFF');

  @override
  void initState() {
    super.initState();
    _checkHardware();
  }

  @override
  void dispose() {
    _mifareKeyController.dispose();
    super.dispose();
  }

  Future<void> _checkHardware() async {
    final support = await NfcPro.checkSupport();
    setState(() {
      _isNfcAvailable = support.isAvailable;
      _isHceSupported = support.isHceSupported;
    });
  }

  Future<void> _initiateSession() async {
    setState(() => _status = "Searching for Tag...");

    try {
      await NfcPro.startSession(
        onDiscovered: (NfcTag tag) {
          setState(() {
            _status = "Tag Detected!";
            _tagData =
                "UID: ${tag.uid}\nType: ${tag.type}\nContent: ${tag.content ?? 'Empty'}";
          });
        },
        onError: (e) {
          setState(() => _status = "Error: ${e.message}");
        },
      );

      // We can also use the stream
      NfcPro.onTagDiscovered.listen((tag) {
        setState(() {
          _tagData = "Live Update: ${tag.uid}";
        });
      });
    } on NfcException catch (e) {
      setState(() => _status = "System Error: ${e.type}");
    }
  }

  Future<void> _secureLoginDemo() async {
    setState(() => _status = "Authenticating...");

    // Simulate a secure challenge-response
    // 1. Start Session
    await NfcPro.startSession(
      onDiscovered: (tag) async {
        // 2. Send APDU Challenge
        final response =
            await NfcPro.transceive("00A404000E325041592E5359532E444446303100");

        if (response != null && response.endsWith("9000")) {
          setState(() => _status = "LOGIN SUCCESS! (Secure APDU)");
        } else {
          setState(() => _status = "Authentication Failed");
        }
      },
    );
  }

  Future<void> _readMifareClassic() async {
    setState(() {
      _status = "Searching for MIFARE Classic tag...";
      _mifareStatus = "Waiting for tag...";
    });

    try {
      await NfcPro.startSession(
        onDiscovered: (tag) async {
          final blocks = await NfcPro.readMifareClassic(keyA: _mifareKeyA);
          if (blocks != null && blocks.isNotEmpty) {
            setState(() {
              _mifareBlocks = blocks;
              _mifareStatus = "Read ${blocks.length} blocks successfully.";
              _status = "MIFARE Classic data loaded.";
            });
          } else {
            setState(() {
              _mifareStatus = "Failed to read MIFARE Classic card.";
              _status = "MIFARE Classic read failed.";
            });
          }
          await NfcPro.stopSession();
        },
        onError: (e) {
          setState(() {
            _mifareStatus = "Tag error: ${e.message}";
            _status = "MIFARE Classic read error.";
          });
        },
      );
    } catch (e) {
      setState(() {
        _mifareStatus = "MIFARE Classic read error: $e";
        _status = "Error reading tag.";
      });
    }
  }

  Future<void> _writeMifareClassic() async {
    if (_mifareBlocks == null || _mifareBlocks!.isEmpty) {
      setState(() {
        _mifareStatus = "No MIFARE data available. Read first.";
      });
      return;
    }

    setState(() {
      _status = "Searching for target tag...";
      _mifareStatus = "Waiting for tag to write clone...";
    });

    try {
      await NfcPro.startSession(
        onDiscovered: (tag) async {
          final success = await NfcPro.writeMifareClassic(
            blocks: _mifareBlocks!,
            keyA: _mifareKeyA,
            startBlock: 0,
          );

          setState(() {
            _mifareStatus = success
                ? "Clone write completed successfully."
                : "Clone write failed.";
            _status = success
                ? "MIFARE Classic clone written."
                : "MIFARE write failed.";
          });
          await NfcPro.stopSession();
        },
        onError: (e) {
          setState(() {
            _mifareStatus = "Tag error: ${e.message}";
            _status = "MIFARE Classic write error.";
          });
        },
      );
    } catch (e) {
      setState(() {
        _mifareStatus = "MIFARE Classic write error: $e";
        _status = "Error writing tag.";
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("NFC Pro Manager"),
        actions: [
          IconButton(onPressed: _checkHardware, icon: const Icon(Icons.refresh))
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Hardware Status Card
            _buildStatusHeader(),
            const SizedBox(height: 20),

            // Main Status Display
            Container(
              padding: const EdgeInsets.all(25),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                    colors: [Colors.deepPurple.shade900, Colors.black]),
                borderRadius: BorderRadius.circular(20),
                border:
                    Border.all(color: Colors.deepPurpleAccent.withAlpha(0x80)),
              ),
              child: Column(
                children: [
                  Text(_status,
                      style: const TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                          color: Colors.cyanAccent)),
                  const Divider(height: 30),
                  Text(_tagData,
                      style: const TextStyle(
                          fontFamily: 'monospace', fontSize: 14)),
                ],
              ),
            ),
            const SizedBox(height: 30),

            // Action Buttons
            ElevatedButton.icon(
              onPressed: _initiateSession,
              icon: const Icon(Icons.nfc),
              label: const Text("Start Professional Session"),
              style:
                  ElevatedButton.styleFrom(padding: const EdgeInsets.all(18)),
            ),
            const SizedBox(height: 12),

            OutlinedButton.icon(
              onPressed: _secureLoginDemo,
              icon: const Icon(Icons.security),
              label: const Text("Secure Login Demo (APDU)"),
              style:
                  OutlinedButton.styleFrom(padding: const EdgeInsets.all(18)),
            ),
            const SizedBox(height: 12),

            const Text("MIFARE CLASSIC CLONE DEMO",
                style: TextStyle(
                    letterSpacing: 2, fontSize: 12, color: Colors.grey)),
            const SizedBox(height: 10),
            TextField(
              controller: _mifareKeyController,
              decoration: const InputDecoration(
                border: OutlineInputBorder(),
                labelText: "MIFARE Key A",
                helperText: "Default is FFFFFFFFFFFF",
              ),
              onChanged: (val) => _mifareKeyA = val.trim(),
            ),
            const SizedBox(height: 10),
            ElevatedButton.icon(
              onPressed: _readMifareClassic,
              icon: const Icon(Icons.download),
              label: const Text("Read MIFARE Classic"),
              style:
                  ElevatedButton.styleFrom(padding: const EdgeInsets.all(18)),
            ),
            const SizedBox(height: 10),
            ElevatedButton.icon(
              onPressed: _writeMifareClassic,
              icon: const Icon(Icons.upload),
              label: const Text("Write MIFARE Classic Clone"),
              style:
                  ElevatedButton.styleFrom(padding: const EdgeInsets.all(18)),
            ),
            const SizedBox(height: 10),
            Text(_mifareStatus,
                style: const TextStyle(fontFamily: 'monospace', fontSize: 14)),
            const SizedBox(height: 30),

            const Text("VIRTUAL CARD CONFIGURATION",
                style: TextStyle(
                    letterSpacing: 2, fontSize: 12, color: Colors.grey)),
            const SizedBox(height: 10),

            TextField(
              decoration: const InputDecoration(
                border: OutlineInputBorder(),
                labelText: "Identity ID to Emulate",
                prefixIcon: Icon(Icons.vibration),
              ),
              onChanged: (val) => NfcPro.setEmulationId(val),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatusHeader() {
    return Row(
      children: [
        _StatusChip(label: "NFC", isActive: _isNfcAvailable),
        const SizedBox(width: 10),
        _StatusChip(label: "HCE", isActive: _isHceSupported),
      ],
    );
  }
}

class _StatusChip extends StatelessWidget {
  final String label;
  final bool isActive;
  const _StatusChip({required this.label, required this.isActive});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: isActive
            ? Colors.green.withAlpha(0x33)
            : Colors.red.withAlpha(0x33),
        borderRadius: BorderRadius.circular(50),
        border: Border.all(color: isActive ? Colors.green : Colors.red),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(isActive ? Icons.check_circle : Icons.error,
              size: 14, color: isActive ? Colors.green : Colors.red),
          const SizedBox(width: 5),
          Text(label,
              style: TextStyle(
                  color: isActive ? Colors.green : Colors.red,
                  fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }
}

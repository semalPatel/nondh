import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        ComposeView()
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainKt.NotesViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

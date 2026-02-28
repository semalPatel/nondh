import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        NotesViewControllerKt.NotesViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

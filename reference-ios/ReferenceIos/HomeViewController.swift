import UIKit

/// Lista de productos (UITableView dentro de un UINavigationController).
final class HomeViewController: UITableViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        // navigationItem.title, no `title`: `title` también renombra la pestaña.
        navigationItem.title = "Reference Shop"
        tableView.register(UITableViewCell.self, forCellReuseIdentifier: "cell")
        let probe = UIBarButtonItem(title: Probe.label, primaryAction: UIAction { [weak self] _ in
            Probe.tap(); self?.navigationItem.rightBarButtonItem?.title = Probe.label
        })
        probe.accessibilityIdentifier = "probe_button"
        navigationItem.rightBarButtonItem = probe
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        navigationItem.rightBarButtonItem?.title = Probe.label
        setPath(Catalog.home)
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        Catalog.products.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let product = Catalog.products[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell", for: indexPath)
        var content = cell.defaultContentConfiguration()
        content.text = "\(product.id). \(product.name) · \(product.price)"
        content.secondaryText = product.note
        cell.contentConfiguration = content
        cell.accessoryType = .disclosureIndicator
        cell.accessibilityIdentifier = "product_\(product.id)"
        return cell
    }

    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        tableView.deselectRow(at: indexPath, animated: true)
        let product = Catalog.products[indexPath.row]
        navigationController?.pushViewController(ProductDetailViewController(product: product), animated: true)
    }
}

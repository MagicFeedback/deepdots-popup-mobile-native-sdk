import { defineConfig } from "astro/config";
import mdx from "@astrojs/mdx";
import starlight from "@astrojs/starlight";

const repository = process.env.GITHUB_REPOSITORY ?? "MagicFeedback/deepdots-popup-mobile-native-sdk";
const repositoryName = repository.split("/")[1] ?? "deepdots-popup-mobile-native-sdk";
const isGitHubActions = process.env.GITHUB_ACTIONS === "true";
const site = process.env.DOCS_SITE_URL ?? "https://magicfeedback.github.io";
const base = isGitHubActions ? `/${repositoryName}/` : "/";

export default defineConfig({
  site,
  base,
  integrations: [
    starlight({
      title: "Deepdots Popup SDK",
      description:
        "Business and technical documentation for the Deepdots Popup SDK on Android and iOS.",
      disable404Route: true,
      customCss: ["./src/styles/custom.css"],
      social: [
        {
          icon: "github",
          label: "GitHub",
          href: "https://github.com/MagicFeedback/deepdots-popup-mobile-native-sdk"
        }
      ],
      locales: {
        root: {
          label: "English",
          lang: "en"
        },
        es: {
          label: "Español",
          lang: "es"
        },
        da: {
          label: "Dansk",
          lang: "da"
        }
      },
      defaultLocale: "root",
      sidebar: [
        {
          label: "Overview",
          items: [
            { label: "Home", link: "/" },
            { label: "Installation", link: "/installation/" },
            { label: "Quickstart", link: "/quickstart/" }
          ]
        },
        {
          label: "Guides",
          items: [
            { label: "Server Mode", link: "/guides/server-mode/" },
            { label: "Client Mode", link: "/guides/client-mode/" },
            { label: "Examples", link: "/guides/examples/" }
          ]
        },
        {
          label: "Reference",
          items: [
            { label: "API Reference", link: "/reference/api-reference/" },
            { label: "Models and Events", link: "/reference/models-and-events/" },
            { label: "Troubleshooting", link: "/troubleshooting/" }
          ]
        }
      ]
    }),
    mdx()
  ]
});

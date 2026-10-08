export default function Loading({ text = 'Loading...' }) {
  return (
    <div className="text-center text-muted py-5">
      <div className="spinner-border text-primary mb-2" role="status" />
      <div>{text}</div>
    </div>
  );
}
